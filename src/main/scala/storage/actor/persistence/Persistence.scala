package storage.actor.persistence

import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.persistence._
import akka.util.Timeout
import storage.actor.utils.ActorLifecycleHooks

import scala.concurrent._
import scala.concurrent.duration._

object Persistence {
  /**
   * Predefined snapshot interval.
   */
  val SnapshotInterval = 1000

  /**
   * Паттерн Publisher-Subscriber.
   * Map with subscribers ActorRefs.
   * Subscribers actors could be inside or outside actor context.
   */
  type Subscribers = Map[UUID, ActorRef]

  /**
   * Команда меняет состояние актора
   */
  trait PersistentCommand

  /**
   * Запрос возвращает данные и не меняет состояние актора
   */
  trait PersistentRequest

  trait PersistentResponse

  trait PersistentEvent

  case object Ping extends PersistentRequest
  case object Kill extends PersistentRequest
  case class CreateChild(x: Props, y: String) extends PersistentRequest
  case class Publish(event: PersistentEvent) extends PersistentRequest

  case object GetSubscriber extends PersistentRequest
  case class AddSubscriber(x: UUID, y: ActorRef) extends PersistentRequest
  case class RemoveSubscriberByActorRef(x: ActorRef) extends PersistentRequest
  case class RemoveSubscriberByUUID(x: UUID) extends PersistentRequest

  case object Pong extends PersistentResponse
  case object Terminated extends PersistentResponse
  case object Success extends PersistentResponse
  case object Persisted extends PersistentResponse
  case object NotPersisted extends PersistentResponse
  case class SubscribersMap(x: Subscribers) extends PersistentResponse

  case class InitializedEvt(state: AnyRef) extends PersistentEvent

  /**
   * Base trait for persistence actor state.
   *
   * @tparam T self type
   */
  trait PersistentState[T <: PersistentState[T]] { self: T =>
    def updated(event: PersistentEvent): T
  }

  /**
   * = Actor in pure functional style =
   *
   * You must set constructor parameters `id`, `initState` and implement `behavior`.
   * When recovering replays events and snapshot, then updates state step by step.
   *
   * - No shared mutable state.
   * - Initial actor state `initState`, very useful.
   * - Автоматически делает snapshot по заданному интервалу.
   * - Создает дочерний актор в своем контексте, если ему прислать Props или (Props, Name).
   * - Обрабатывает ошибки, удобно при использовании supervisor strategy.
   * - Can reply on [[Ping]] request.
   * - Incoming events are persisted.
   * - Terminates on [[Kill]] message and reply [[Terminated]], very useful for async future tests.
   *
   * == Persistent events should be clearly mapped to state entities ==
   *
   * For example, if state stores ProcessInfo entities, there should be events:
   *
   * {{{
   *  case class ProcessRuntimeState(v: Map[ProcessInstance.PersistenceId, ProcessInfo] = Map.empty)
   *
   *  sealed trait Event extends PersistentEvent
   *  case class CreatedProcessInfoEvt(x: UpdateProcessInfo, t: ProcessTemplate.PersistenceId) extends Event
   *  case class UpdatedProcessInfoEvt(x: UpdateProcessInfo) extends Event
   *  case class DeletedProcessInfoEvt(id: ProcessInstance.PersistenceId) extends Event
   * }}}
   *
   * Follow this convention rule for Persistent event names.
   *
   * == Future API ==
   *
   * Use Future Api inside future.
   *
   * == Store ActorRef inside Actor ==
   *
   * Related ActorRefs are not Persisted.
   * To add related ActorRef use method: [[PersistentStateActor#addRelated]].
   * To remove related ActorRef use method: [[PersistentStateActor#removeRelated]].
   *
   * == Tests ==
   *
   * Use PersistenceSpec as base trait for idiomatic persistence tests.
   *
   */
  trait PersistentStateActor[T <: PersistentState[T]] extends PersistentActor
    with ActorLogging
    with ActorLifecycleHooks
    with DefaultBehaviors {

    private var recoveryOpt: Option[T] = None
    private var subscribers: Subscribers = Map.empty

    def id: PersistenceId
    def persistenceId: String = id.pathStr

    def initState: T

    /**
     * Поведение актора которое необходимо реализовать.
     */
    def behavior(state: T): Receive

    def snapshotInterval: Int = SnapshotInterval

    def afterRecover(state: T): Unit = {}

    def afterSnapshot(metadata: SnapshotMetadata, success: Boolean): Unit = {}

    /**
     * === Поведение актора ===
     *
     * Поведение актора - это композиция из PartialFunction
     */

    def recoverFromSnapshotBehavior: Receive = {
      case m @ SaveSnapshotSuccess(SnapshotMetadata(pid, sequenceNr, timestamp)) =>
        log.info(s"New snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}}} saved")
        afterSnapshot(m.metadata, success = true)
      case m @ SaveSnapshotFailure(SnapshotMetadata(pid, sequenceNr, timestamp), reason) =>
        log.error(
          s"""Saving snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}}} failed
             |reason: $reason
           """.stripMargin)
        afterSnapshot(m.metadata, success = false)
    }

    /**
     * Поведение: Повторение событий при восстановлении и получении новых событий.
     *
     * Повторяет события при восстановлении актора, меняя состояние в функциональном стиле.
     */
    def persistBehavior(state: T): Receive = {
      case evt: PersistentEvent => persist(evt) { event =>
        changeState(state.updated(event))
        replyPersisted()
      }
    }

    def subscribersBehavior: Receive = {
      case GetSubscriber =>
        sender() ! SubscribersMap(subscribers)
      case AddSubscriber(x, y) =>
        subscribers += x -> y
        replySuccess()
      case RemoveSubscriberByUUID(x) =>
        subscribers -= x
        replySuccess()
      case RemoveSubscriberByActorRef(x) =>
        subscribers = subscribers.filterNot(_._2 == x)
        replySuccess()
    }

    /**
     * Поведение: Автоматическое создание снимка состояния.
     *
     * По заданному интервалу делаем snapshot.
     * Проверку делаем только для команды записи.
     */
    def saveSnapshotBehavior(state: T): PartialFunction[Any, Any] = {
      case cmd: PersistentCommand if lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0 =>
        saveSnapshot(state)
        cmd
      case cmd => cmd
    }

    /**
     * Поведение: Оповещение о новом [[PersistentEvent]] событии.
     *
     * Незаменимый механизм при тестировании акторов с футурами.
     * Используется для тестирования движка бизнес-процессов.
     */
    def publishBehavior: Receive = {
      case Publish(event) =>
        publishEvt(event)
        replySuccess()
    }

    /**
     * Активный контекст актора.
     */
    def active(state: T): Receive =
      saveSnapshotBehavior(state)
        .andThen { cmd =>
          behavior(state)
            .orElse(recoverFromSnapshotBehavior)
            .orElse(creatorBehavior)
            .orElse(persistBehavior(state))
            .orElse(subscribersBehavior)
            .orElse(publishBehavior)
            .orElse(throwableBehavior)
            .orElse(echoBehavior)
            .orElse(terminateBehavior)
            .orElse(notMatchedBehavior)(cmd)
        }

    /**
     * Обновление состояния актора
     */
    def changeState(state: T): Unit = context.become(active(state))

    def receiveCommand: Receive = active(initState)

    def receiveRecover: Receive = {
      case s: T @unchecked =>
        recoveryOpt = Option(s)
        changeState(s)
        log.info("Initialization completed")
      case event: PersistentEvent =>
        recoveryOpt = recoveryOpt.map(_.updated(event))
        changeState(recoveryOpt.get)
      case SnapshotOffer(SnapshotMetadata(pid, sequenceNr, timestamp), snapshot: T @unchecked) =>
        recoveryOpt = Option(snapshot)
        changeState(snapshot)
        log.info(s"Snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}} offered")
      case RecoveryCompleted =>
        val s = recoveryOpt.getOrElse(initState)
        if (recoveryOpt.nonEmpty) recoveryOpt = None
        else persist(initState) { _ => }
        log.info("Recovery completed")
        afterRecover(s)
    }

    def d(timestamp: Long): String =
      LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString

    def replyPersisted(): Unit = sender() ! Persisted

    def replySuccess(): Unit = if (sender() != self) sender() ! Success

    /**
     * === Publisher-Subscriber ===
     *
     * Единый механизм в функциональном стиле для работы со связанными акторами.
     * Используется вместо переменной var со списком ActorRefs.
     */
    def subscribersMap: Subscribers = subscribers

    def subscribersRefs: Seq[ActorRef] = subscribers.values.toSeq

    def addSubscriber(selfRef: ActorRef, x: UUID, y: ActorRef): Unit =
      selfRef ! AddSubscriber(x, y)

    def removeSubscriber(selfRef: ActorRef, x: ActorRef): Unit =
      selfRef ! RemoveSubscriberByActorRef(x)

    def removeSubscriber(selfRef: ActorRef, x: UUID): Unit =
      selfRef ! RemoveSubscriberByUUID(x)

    /**
     * Publish event to main event bus.
     * Very useful for tests.
     *
     * @param event persistent event
     */
    def publishEvt(event: PersistentEvent): Unit = context.system.eventStream.publish(event)

    // === Future API ===

    def publishEvt(selfRef: ActorRef, event: PersistentEvent)(implicit t: Timeout): Future[PersistentResponse] =
      ask(selfRef, Publish(event))
        .mapTo[PersistentResponse]

    def createChild(selfRef: ActorRef, props: Props)(implicit t: Timeout): Future[ActorRef] =
      ask(selfRef, props)
        .mapTo[ActorRef]

    def createChild(selfRef: ActorRef, props: Props, name: String)(implicit t: Timeout): Future[ActorRef] =
      ask(selfRef, (props, name))
        .mapTo[ActorRef]

    def createChild(selfRef: ActorRef, x: (Props, String))(implicit t: Timeout): Future[ActorRef] =
      ask(selfRef, (x._1, x._2))
        .mapTo[ActorRef]

    /**
     * Persist event in Future.
     */
    def persistEvt(selfRef: ActorRef, event: PersistentEvent)(implicit e: ExecutionContext, t: Timeout): Future[PersistentEvent] =
      ask(selfRef, event)
        .map(_ => event)

    /**
     * Send async command in future.
     * Useful when actor doesn't responding.
     */
    def sendCmd(toRef: ActorRef, cmd: PersistentCommand)(implicit e: ExecutionContext): Future[PersistentResponse] =
      Future {
        toRef ! cmd
        Success
      }

    override def postStop(): Unit = {
      terminateOpt.foreach {
        // Required to correctly shutdown persistence layer
        case (x, y) => context.system.scheduler.scheduleOnce(50 milliseconds, x, y)(context.dispatcher)
      }
      log.info("Terminated")
    }
  }

  /**
   * = In memory Persistent Actor in pure functional style =
   */
  trait InMemoryPersistentStateActor[T <: PersistentState[T]] extends Actor
    with ActorLogging
    with ActorLifecycleHooks
    with DefaultBehaviors {

    def id: String
    def initState: T

    /**
     * Поведение актора которое необходимо реализовать.
     */
    def behavior(state: T): Receive

    def active(state: T): Receive =
      behavior(state)
        .orElse(creatorBehavior)
        .orElse(throwableBehavior)
        .orElse(echoBehavior)
        .orElse(terminateBehavior)
        .orElse(notMatchedBehavior)

    def changeState(state: T): Unit = context.become(active(state))

    def receive: Receive = active(initState)
  }
}
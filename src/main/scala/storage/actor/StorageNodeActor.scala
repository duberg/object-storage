package storage.actor

import java.time.ZonedDateTime

import akka.actor.{ ActorRef, Props }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import storage.Storage
import storage.Storage.{ GetBoolean, GetInt, GetString }
import storage.actor.persistence.Persistence
import storage.actor.persistence.Persistence._

import scala.concurrent.{ ExecutionContext, Future }

class StorageNodeActor(
  val id: String,
  val initState: StorageNodeState)(implicit val c: ExecutionContext, val t: Timeout) extends PersistentStateActor[StorageNodeState] {
  def behavior(state: StorageNodeState) = {
    case GetInt(path) => state.storage.getInt(path)
    case GetString(path) => state.storage.getString(path)
    case GetBoolean(path) => state.storage.getBoolean(path)
  }
}

object StorageNodeActor {
  trait Command extends PersistentCommand
  trait Request extends PersistentRequest
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  //  case class CreateProcessCmd(x: CreateProcessInfo, modifier: Modifier) extends Command
  //  case class StartProcessCmd(processId: Process.Id, modifier: Modifier) extends Command
  //  case class CompleteProcessCmd(processId: Process.Id, modifier: Modifier) extends Command
  //  case class UpdateProcessCmd(x: UpdateProcessInfo, modifier: Modifier) extends Command
  //  case class DeleteProcessCmd(processId: Process.Id, modifier: Modifier) extends Command
  //
  //  case class GetProcessInfoById(processId: Process.Id) extends Request
  //  case object GetProcessInfoAll extends Request
  //  case class GetProcessExtendedInfo(processId: Process.Id) extends Request
  //  case class GetProcessStatus(processId: Process.Id) extends Request
  //  case class Forward(processId: Process.Id, operationId: Operation.Id, message: Any) extends Request
  //
  //  case class CreatedProcessInfoEvt(x: ProcessInfo) extends Event
  //  case class UpdatedProcessInfoEvt(x: UpdateProcessInfo, modifier: Modifier) extends Event
  //  case class DeletedProcessInfoEvt(processId: Process.Id) extends Event
  //
  //  case object Done extends Response
  //  case object ProcessNotFound extends Response
  //  case class CreateProcessSuccess(x: ProcessInfo) extends CreateProcessResponse
  //  case class CreateProcessFailure(x: List[BpmException]) extends CreateProcessResponse
  //  case class ProcessInfoOpt(x: Option[ProcessInfo]) extends Response
  //  case class ProcessInfoMap(x: Map[Process.Id, ProcessInfo]) extends Response
  //  case class ProcessExtendedInfoOpt(x: Option[ProcessExtendedInfo]) extends Response
  //  case class ProcessStatusOpt(x: Option[ProcessStatus]) extends Response

  //  def props(
  //             id: ProcessManager.Id,
  //             masterDataManager: MasterDataManager,
  //             taskManager: TaskManager,
  //             historyManager: HistoryManager,
  //             notificationManager: NotificationManager,
  //             state: ProcessManagerState = ProcessManagerState.empty)(implicit c: ExecutionContext, t: Timeout) =
  //    Props(new ProcessManagerActor(
  //      id = id,
  //      masterDataManager = masterDataManager,
  //      taskManager = taskManager,
  //      historyManager = historyManager,
  //      notificationManager = notificationManager,
  //      initState = state))
}

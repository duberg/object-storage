package storage.actor.persistence

import akka.actor.{Actor, ActorRef, Props}
import storage.actor.persistence.Persistence._

trait DefaultBehaviors { _: Actor =>
  protected var terminateOpt: Option[(ActorRef, Any)] = None

  def throwableBehavior: Receive = { case e: Exception => throw e }

  def creatorBehavior: Receive = {
    case p: Props =>
      val a = context.actorOf(p)
      sender() ! a
    case (props: Props, name: String) =>
      val a = context.actorOf(props, name)
      sender() ! a
  }

  def echoBehavior: Receive = {
    case "ping" => sender() ! "pong"
    case Ping => sender() ! Pong
  }

  /**
   * Поведение: Правильное завершение актора.
   *
   * Необходимо использовать в тестах.
   */
  def terminateBehavior: Receive = {
    case "kill" =>
      context.stop(self)
      terminateOpt = Option(sender() -> "terminated")
    case Kill =>
      context.stop(self)
      terminateOpt = Option(sender() -> Terminated)
  }

  def notMatchedBehavior: Receive = {
    case _ =>
  }
}
package storage

import akka.actor.ActorRef
import akka.util.Timeout

import scala.concurrent.{ ExecutionContext, Future }

class StorageSystem(val storageSystemActor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) extends StorageSystemLike

object StorageSystem {
  def apply(storageActor: ActorRef)(implicit c: ExecutionContext, t: Timeout): StorageSystem = new StorageSystem(storageActor)
}
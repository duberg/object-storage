package storage

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import storage.StorageSystem._
import storage.actor.persistence.PersistenceId

import scala.concurrent.{ExecutionContext, Future}

trait StorageSystemLike extends StorageNode {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  def storageSystemActor: ActorRef

  def createNode(x: CreateNode): Future[PersistenceId] =
    ask(storageSystemActor, CreateNodeCmd(x))
      .mapTo[NodeIdOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("StorageNode already exists")
      }
}

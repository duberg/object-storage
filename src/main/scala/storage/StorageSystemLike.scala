package storage

import akka.actor.ActorRef
import akka.util.Timeout
import StorageSystemLike._

import scala.concurrent.{ ExecutionContext, Future }
import akka.pattern.ask
import storage.actor.StorageSystemActor
import storage.actor.persistence.PersistenceId
import storage.actor.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentRequest, PersistentResponse }

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

object StorageSystemLike {
  trait Command extends PersistentCommand
  trait Request extends PersistentRequest
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class CreateNodeCmd(x: CreateNode) extends Command
  case class UpdateNodeCmd(x: UpdateNodeInfo) extends Command
  case class DeleteNodeCmd(nodeId: PersistenceId) extends Command

  case class GetNodeInfoById(nodeId: PersistenceId) extends Request
  case object GetNodeInfoAll extends Request
  case class GetNodeStatus(nodeId: PersistenceId) extends Request

  case class CreatedNodeInfoEvt(x: CreateNode) extends Event
  case class UpdatedNodeInfoEvt(x: UpdateNodeInfo) extends Event
  case class DeletedNodeInfoEvt(nodeId: PersistenceId) extends Event

  case class NodeIdOpt(x: Option[PersistenceId]) extends Response
  case class NodeInfoOpt(x: Option[NodeInfo]) extends Response
  case class NodeInfoMap(x: Map[String, NodeInfo]) extends Response
  case class NodeStatusOpt(x: Option[StorageStatus]) extends Response
}

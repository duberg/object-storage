package storage

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import storage.StorageSystem._
import storage.actor.persistence.PersistenceId

import scala.concurrent.{ ExecutionContext, Future }

trait StorageSystemLike extends StorageNode {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  def storageSystem: ActorRef

  def createNode(x: CreateNode): Future[PersistenceId] =
    ask(storageSystem, CreateNodeCmd(x))
      .mapTo[NodeIdOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Node already exists")
      }

  def getNodeInfo(nodeId: PersistenceId): Future[NodeInfo] =
    ask(storageSystem, GetNodeInfoById(nodeId))
      .mapTo[NodeInfoOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Node not found")
      }

  def getNodeInfoAll: Future[Map[String, NodeInfo]] =
    ask(storageSystem, GetNodeInfoAll)
      .mapTo[NodeInfoMap]
      .map(_.x)

  def getNodeStatus(nodeId: PersistenceId): Future[StorageStatus] =
    ask(storageSystem, GetNodeStatus(nodeId))
      .mapTo[NodeStatusOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Node not found")
      }
}

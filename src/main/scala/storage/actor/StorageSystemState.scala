package storage.actor

import storage._
import storage.actor.persistence.Persistence._
import StorageSystemLike._
import storage.actor.persistence.PersistenceId

case class StorageSystemState(v: Map[PathStr, NodeInfo]) extends PersistentState[StorageSystemState] {
  def createNodeInfo(x: CreateNode): StorageSystemState = {
    val info = NodeInfo(
      id = x.id,
      description = x.description,
      name = x.name,
      status = StorageStatus.Created)
    copy(v + (x.id.pathStr -> info))
  }

  def updateNodeInfo(x: UpdateNodeInfo): StorageSystemState = {
    val y = v(x.id.pathStr)
    val info = NodeInfo(
      id = y.id,
      name = x.name.getOrElse(y.name),
      description = x.description.getOrElse(y.description),
      status = x.status.getOrElse(y.status))
    copy(v + (x.id.pathStr -> info))
  }

  def deleteNodeInfo(x: PersistenceId): StorageSystemState = copy(v - x.pathStr)

  def nodeInfoExists(x: PersistenceId): Boolean = v.get(x.pathStr).isDefined

  def getNodeInfo(x: PersistenceId): NodeInfo = v(x.pathStr)

  def getNodeInfoOpt(storageId: PersistenceId): Option[NodeInfo] = v.get(storageId.pathStr)

  def getNodeInfoAll: Map[String, NodeInfo] = v

  def getNodeIds: Iterable[PersistenceId] = v.keys.map(PersistenceId.apply)

  def updated(event: PersistentEvent): StorageSystemState = event match {
    case CreatedNodeInfoEvt(x) => createNodeInfo(x)
    case UpdatedNodeInfoEvt(x) => updateNodeInfo(x)
    case DeletedNodeInfoEvt(x) => deleteNodeInfo(x)
  }
}

object StorageSystemState {
  def empty = StorageSystemState(Map.empty)
}

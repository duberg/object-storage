package storage.actor

import akka.actor.Props
import storage.StorageSystem._
import storage._
import storage.actor.persistence.Persistence._
import storage.actor.persistence.PersistenceId

class StorageSystemActor(val id: PersistenceId, val initState: StorageSystemState) extends PersistentStateActor[StorageSystemState] {
  def createNode(state: StorageSystemState, x: CreateNode): Unit = {
    if (!state.nodeInfoExists(x.id)) {
      context.actorOf(StorageNodeActor.props(id = x.id, state = StorageNodeState(x.storage)), x.id.name)
      persist(CreatedNodeInfoEvt(x)) { event =>
        changeState(state.updated(event))
        sender() ! NodeIdOpt(Option(x.id))
        log.info(s"Node ${x.id.name} created")
      }
    } else sender() ! NodeIdOpt(None)
  }

  def findNodeInfoById(state: StorageSystemState, nodeId: PersistenceId): Unit = sender() ! NodeInfoOpt(state.getNodeInfoOpt(nodeId))

  def findNodeInfoAll(state: StorageSystemState): Unit = sender() ! NodeInfoMap(state.getNodeInfoAll)

  def findNodeStatus(state: StorageSystemState, nodeId: PersistenceId): Unit = sender() ! NodeStatusOpt(state.getNodeInfoOpt(nodeId).map(_.status))

  def forwardRequest(x: Storage.Request): Unit = context.child(x.path.nodeName).get forward x

  def forwardCommand(x: Storage.Command): Unit = context.child(x.path.nodeName).get forward x

  def behavior(state: StorageSystemState) = {
    case x: Storage.Request => forwardRequest(x)
    case x: Storage.Command => forwardCommand(x)
    case GetNodeInfoById(x) => findNodeInfoById(state, x)
    case GetNodeInfoAll => findNodeInfoAll(state)
    case GetNodeStatus(x) => findNodeStatus(state, x)
    case CreateNodeCmd(x) => createNode(state, x)
  }

  override def afterRecover(state: StorageSystemState): Unit = {
    state.getNodeIds.foreach { nodeId =>
      context.actorOf(StorageNodeActor.props(nodeId), nodeId.name)
      log.info(s"Node ${nodeId.name} recovered")
    }
  }
}

object StorageSystemActor {
  def props(id: PersistenceId, state: StorageSystemState = StorageSystemState.empty) = Props(new StorageSystemActor(id, state))
}
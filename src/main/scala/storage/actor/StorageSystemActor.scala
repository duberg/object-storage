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

  def behavior(state: StorageSystemState) = {
    case GetNodeRef(x) => sender() ! NodeRefOpt(context.child(x.name))
    case GetNodeInfoById(x) => sender() ! NodeInfoOpt(state.getNodeInfoOpt(x))
    case GetNodeInfoAll => sender() ! NodeInfoMap(state.getNodeInfoAll)
    case GetNodeStatus(x) => sender() ! NodeStatusOpt(state.getNodeInfoOpt(x).map(_.status))
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
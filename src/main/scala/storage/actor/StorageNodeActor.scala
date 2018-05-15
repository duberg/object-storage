package storage.actor

import akka.actor.Props
import storage.Storage.{ GetBoolean, GetInt, GetString }
import storage._
import storage.actor.persistence.Persistence._
import Storage._
import storage.actor.persistence.PersistenceId

class StorageNodeActor(
  val id: PersistenceId,
  val initState: StorageNodeState) extends PersistentStateActor[StorageNodeState] {
  def behavior(state: StorageNodeState) = {
    case GetInt(path) => sender() ! IntOpt(Option(state.storage.getInt(path)))
    case GetString(path) => sender() ! StringOpt(Option(state.storage.getString(path)))
    case GetBoolean(path) => sender() ! BooleanOpt(Option(state.storage.getBoolean(path)))
    case GetDecimal(path) => sender() ! DecimalOpt(Option(state.storage.getDecimal(path)))
    case GetElement(path) => sender() ! ElementOpt(Option(state.storage.getElement(path)))
  }
}

object StorageNodeActor {
  def props(id: PersistenceId, state: StorageNodeState = StorageNodeState.empty) = Props(new StorageNodeActor(id, state))
}

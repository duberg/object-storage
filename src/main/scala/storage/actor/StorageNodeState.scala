package storage.actor

import storage.Storage
import storage.Storage._
import storage.actor.persistence.Persistence
import storage.actor.persistence.Persistence.PersistentState

case class StorageNodeState(storage: Storage) extends PersistentState[StorageNodeState] {
  def updated(event: Persistence.PersistentEvent): StorageNodeState = event match {
    case UpdatedDataElementEvt(x) => copy(storage.updateDataElement(x))
    case UpdatedDataEvt(x) => copy(storage.updateData(x))
    case UpdatedElementEvt(x, y) => copy(storage.updateElement(x, y))
  }
}

object StorageNodeState {
  def empty: StorageNodeState = StorageNodeState(Storage.empty)
}
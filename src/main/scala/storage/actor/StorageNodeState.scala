package storage.actor

import storage.actor.persistence.Persistence
import storage.actor.persistence.Persistence.PersistentState
import storage.{ Storage, StorageLike }
import Storage._

case class StorageNodeState(storage: Storage) extends PersistentState[StorageNodeState] {
  def updated(event: Persistence.PersistentEvent): StorageNodeState = event match {
    case _: Persistence.PersistentEvent => this
  }
}

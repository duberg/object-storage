package storage.actor

import akka.actor.Props
import storage.Storage.{GetBoolean, GetInt, GetString, _}
import storage._
import storage.actor.persistence.Persistence._
import storage.actor.persistence.PersistenceId

import scala.util.{Failure, Success, Try}

class StorageNodeActor(
  val id: PersistenceId,
  val initState: StorageNodeState) extends PersistentStateActor[StorageNodeState] {

  private def update(state: StorageNodeState, event: Event): Unit = {
    Try(state.updated(event)) match {
      case x @ Success(updatedState) =>
        persist(event) { event =>
          changeState(updatedState)
          sender() ! UpdatedStorage(x.map(_.storage))
        }
      case Failure(e) => sender() ! UpdatedStorage(Failure(e))
    }
  }

  def behavior(state: StorageNodeState) = {
    case GetInt(path) => sender() ! IntOpt(Option(state.storage.getInt(path)))
    case GetString(path) => sender() ! StringOpt(Option(state.storage.getString(path)))
    case GetBoolean(path) => sender() ! BooleanOpt(Option(state.storage.getBoolean(path)))
    case GetDecimal(path) => sender() ! DecimalOpt(Option(state.storage.getDecimal(path)))
    case GetElement(path) => sender() ! ElementOpt(Option(state.storage.getElement(path)))
    case GetRoot(_) => sender() ! Root(state.storage.root)
    case UpdateDataElementCmd(x, path) => update(state, UpdatedDataElementEvt(x))
  }
}

object StorageNodeActor {
  def props(id: PersistenceId, state: StorageNodeState = StorageNodeState.empty) = Props(new StorageNodeActor(id, state))
}

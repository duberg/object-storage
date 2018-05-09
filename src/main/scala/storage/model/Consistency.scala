package storage.model

trait Consistency

object Consistency {
  case object Disabled extends Consistency
  case object Strict extends Consistency
}
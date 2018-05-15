package storage

sealed trait StorageStatus

object StorageStatus {
  case object Created extends StorageStatus
  case object Started extends StorageStatus
  case object Completed extends StorageStatus
  case object Deleted extends StorageStatus
  case object Failed extends StorageStatus
}
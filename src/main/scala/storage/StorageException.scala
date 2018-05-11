package storage

case class StorageException(message: String = "", cause: Option[Exception] = None) extends RuntimeException(message, cause.orNull)


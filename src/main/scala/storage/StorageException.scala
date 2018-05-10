package storage

case class StorageException(pathStr: PathStr, message: String = "", cause: Option[Exception] = None) extends RuntimeException(message, cause.orNull)

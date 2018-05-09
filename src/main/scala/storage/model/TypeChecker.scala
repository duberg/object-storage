package storage.model

trait TypeChecker {
  def checkType(path: PathStr, x: ReprElement, y: Any): Unit = {
    val clsX = x.getClass
    val clsY = y.getClass
    (x, y) match {
      case (x: ObjectMetadata, y: ObjectDefinition) =>
      case (x: CollectionMetadata, y: CollectionDefinition) =>
      case _ => if (clsX != clsY) throw StorageException(path, s"Invalid type $clsY, require $clsX")
    }
  }
}

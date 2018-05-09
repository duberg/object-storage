package storage.model

trait TypeChecker {
  def checkType(path: PathStr, x: ReprElement, y: Any): Unit = {
    val clsX = x.getClass
    val clsY = y.getClass
    (x, y) match {
      case (x: ObjectMetadata, y: ObjectDefinition) =>
      case (x: ArrayMetadata, y: ArrayDefinition) =>
      case _ => if (clsX != clsY) throw StorageException(path, s"Invalid type $clsY, require $clsX")
    }
  }
}

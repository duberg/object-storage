package storage

trait TypeChecker {
  def checkType(path: PathStr, x: ReprElement, y: Any): Unit = {
    val clsX = x.getClass
    val clsY = y.getClass
    (x, y) match {
      case (x: ObjectMetadata, y: ObjectElement) =>
      case (x: ArrayElement, y: ArrayElement) =>
      case _ => if (clsX != clsY) throw StorageException(s"Invalid type $clsY, require $clsX")
    }
  }
}

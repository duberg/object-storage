package storage

import scala.concurrent.Future

class StorageManager  {
  def getBoolean(path: Path): Future[Boolean] = ???

  def getInt(path: Path) = ???

  def getString(path: Path) = ???

  def getDecimal(path: Path) = ???

  def getElement(path: Path) = ???

  def getComplexElement(path: Path) = ???

  def getObjectElement(path: Path) = ???

  def getArrayElement(path: Path) = ???

  def getDataElement(path: Path) = ???

  def getData(paths: Paths) = ???

  def updateElement(path: Path, definition: AnyElement, consistency: Consistency) = ???

  def updateDataElement(x: DataElement) = ???

  def updateData(x: Data) = ???

  def updateData(x: (PathStr, Value)*) = ???

  def updateElement(path: PathStr, X: AnyElement, consistency: Consistency) = ???

  def createElement(path: Path, x: AnyElement) = ???

  def createElement(path: PathStr, x: AnyElement) = ???

  def createElement(x: AnyElement) = ???

  def deleteElement(path: Path) = ???

  def prettify = ???
}

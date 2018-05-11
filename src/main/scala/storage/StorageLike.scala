package storage

trait StorageLike extends Printable {
  def apply(path: Path): AnyElement
  def apply(path: PathStr): AnyElement = apply(Path(path))

  def repr: Repr
  def root: ObjectElement

  def getBoolean(path: Path): Boolean
  def getInt(path: Path): Int
  def getString(path: Path): String
  def getDecimal(path: Path): BigDecimal
  def getElement(path: Path): AnyElement
  def getComplexElement(path: Path): ComplexElement
  def getObjectElement(path: Path): ObjectElement
  def getArrayElement(path: Path): ArrayElement
  def getArrayElement(path: PathStr): ArrayElement = getArrayElement(Path(path))
  def getDataElement(path: Path): DataElement
  def getData(paths: Paths): Data
  def getBoolean(path: PathStr): Boolean = getBoolean(Path(path))
  def getInt(path: PathStr): Int = getInt(Path(path))
  def getString(path: PathStr): String = getString(Path(path))
  def getDecimal(path: PathStr): BigDecimal = getDecimal(Path(path))
  def getElement(path: PathStr): AnyElement = getElement(Path(path))
  def getDataElement(path: PathStr): DataElement = getDataElement(Path(path))
  def getData(paths: List[PathStr]): Data = getData(Paths.fromPathStrs(paths))

  def updateElement(path: Path, definition: AnyElement, consistency: Consistency): StorageLike
  def updateDataElement(x: DataElement): StorageLike
  def updateData(x: Data): StorageLike
  def updateData(x: (PathStr, Value)*): StorageLike
  def updateElement(path: PathStr, X: AnyElement, consistency: Consistency = Consistency.Strict): StorageLike

  def createElement(path: Path, x: AnyElement): StorageLike
  def createElement(path: PathStr, x: AnyElement): StorageLike
  def createElement(x: AnyElement): StorageLike

  def deleteElement(path: Path): StorageLike
}
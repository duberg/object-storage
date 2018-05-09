package storage.model

trait StorageLike extends Printable { self =>
  def repr: Repr

  def apply(path: Path): AnyDefinition

  def apply(path: PathStr): AnyDefinition = apply(Path(path))

  def getBoolean(path: Path): Boolean

  def getInt(path: Path): Int

  def getString(path: Path): String

  def getDecimal(path: Path): BigDecimal

  def getDefinition(path: Path): AnyDefinition

  def getDataElement(path: Path): DataElement

  def getData(paths: Paths): Data

  def updateDefinition(path: Path, definition: AnyDefinition, consistency: Consistency): StorageLike

  def updateDataElement(x: DataElement): StorageLike

  def updateData(x: Data): StorageLike

  def addDefinition(path: Path, definition: AnyDefinition): StorageLike = updateDefinition(path, definition, Consistency.Disabled)

  def getBoolean(path: PathStr): Boolean = getBoolean(Path(path))

  def getInt(path: PathStr): Int = getInt(Path(path))

  def getString(path: PathStr): String = getString(Path(path))

  def getDecimal(path: PathStr): BigDecimal = getDecimal(Path(path))

  def getDefinition(path: PathStr): AnyDefinition = getDefinition(Path(path))

  def getDataElement(path: PathStr): DataElement = getDataElement(Path(path))

  def getData(paths: List[PathStr]): Data = getData(Paths.fromPathStrs(paths))

  def updateData(x: (PathStr, Value)*): StorageLike = x match {
    case Seq() => self
    case Seq((path, value)) => updateDataElement(DataElement(path, value))
    case Seq(a, as @ _*) => updateData(Data(x.toMap))
  }

  def updateDefinition(path: PathStr, definition: AnyDefinition, consistency: Consistency = Consistency.Strict): StorageLike = updateDefinition(Path(path), definition, consistency)

  def addDefinition(path: PathStr, definition: AnyDefinition): StorageLike = addDefinition(Path(path), definition)

  def addDefinition(definition: AnyDefinition): StorageLike
}

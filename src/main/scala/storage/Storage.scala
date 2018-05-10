package storage

/**
 * = Object storage =
 *
 *  - Data consistency
 *  - Fast read and write
 *  - Fast akka persistence serialization
 */
case class Storage(repr: Repr = Repr.empty) extends StorageLike {
  def apply(path: Path): AnyElement = repr(path.pathStr)

  def getBoolean(path: Path): Boolean = repr(path.pathStr) match {
    case x: BooleanElement => x.value
    case x: StringElement => x.value.toBoolean
  }

  def getInt(path: Path): Int = repr(path.pathStr) match {
    case x: IntElement => x.value
    case x: StringElement => x.value.toInt
  }

  def getString(path: Path): String = repr(path.pathStr) match {
    case x: StringElement => x.value
    case x => x.toString
  }

  def getDecimal(path: Path): BigDecimal = repr(path.pathStr) match {
    case x: DecimalElement => x.value
    case x: IntElement => x.value
    case x: StringElement => x.value.toInt
  }

  def getElement(path: Path): AnyElement = apply(path)

  def getComplexElement(path: Path): ComplexElement = repr.getComplexElement(repr.getMetadata(path.pathStr))

  def getObjectElement(path: Path): ObjectElement = repr.getObjectElement(repr.getObjectMetadata(path.pathStr))

  def getArrayElement(path: Path): ArrayElement = repr.getArrayElement(repr.getArrayMetadata(path.pathStr))

  def getDataElement(path: Path): DataElement = DataElement(path, apply(path).value)

  def getData(paths: Paths): Data = Data(paths.map(path => DataElement(path, apply(path).value)).toSet)

  def updateElement(path: Path, definition: AnyElement, consistency: Consistency): Storage = copy(repr.updateElement(path.pathStr, definition, consistency))

  def updateDataElement(x: DataElement): Storage = copy(repr.updateValue(x.path.pathStr, x.value))

  def updateData(x: Data): Storage = copy((repr /: x.elements) { case (r, d) => r.updateValue(d.path.pathStr, d.value) })

  def updateData(x: (PathStr, Value)*): Storage = x match {
    case Seq() => this
    case Seq((path, value)) => updateDataElement(DataElement(path, value))
    case Seq(a, as @ _*) => updateData(Data(x.toMap))
  }

  def addElement(x: AnyElement): Storage = copy(repr.addElement(x))

  def addElement(path: Path, definition: AnyElement): Storage = copy(repr.addElement(path.pathStr, definition))

  def paths: Paths = Paths(repr.impl.keys.toList.map(Path.apply))

  def root: ObjectElement = {
    val rootImpl = repr.impl.map({ case (path, reprElem) => s"$$.$path" -> reprElem.withPath(s"$$.$path") })
    val metadata = ObjectMetadata(Some("root"), None, "$")
    val root = storage.Repr(rootImpl + ("$" -> metadata))
    root.getObjectElement(metadata)
  }

  def prettify: String = s"[${getClass.getSimpleName}]".yellow + root.prettify

  def updateElement(path: PathStr, x: AnyElement, consistency: Consistency = Consistency.Strict): Storage = updateElement(Path(path), x, consistency)

  def addElement(path: PathStr, x: AnyElement): StorageLike = addElement(Path(path), x)

  def addDataElement(x: DataElement) = ???

  def addData(x: Data) = ???

  def addData(x: (PathStr, Value)*): StorageLike = x match {
    case Seq() => this
    case Seq((path, value)) => addDataElement(DataElement(path, value))
    case Seq(a, as @ _*) => addData(Data(x.toMap))
  }

  repr.impl.foreach({
    case (path, reprElement) => require(path == reprElement.path, s"Invalid reprElement $reprElement: require same path")
  })
}

object Storage {
  def empty: Storage = Storage(Repr.empty)
  def apply(x: Map[PathStr, ReprElement]): Storage = Storage(storage.Repr(x))
  def apply(x: ReprElement*): Storage = Storage(Repr(x.map(r => r.path -> r).toMap))
}
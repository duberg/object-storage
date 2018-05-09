package storage.model

/**
 * = Хранилище определений =
 *
 *  - Согласованные данные
 *  - Быстрые операции чтения и записи
 *  - Простая и быстрая сериализация данных
 */
case class Storage(repr: Repr = Repr.empty) extends StorageLike { self =>
  def apply(path: Path): AnyDefinition = repr(path.pathStr)

  def getBoolean(path: Path): Boolean = repr(path.pathStr) match {
    case x: BooleanDefinition => x.value
    case x: StringDefinition => x.value.toBoolean
  }

  def getInt(path: Path): Int = repr(path.pathStr) match {
    case x: IntDefinition => x.value
    case x: StringDefinition => x.value.toInt
  }

  def getString(path: Path): String = repr(path.pathStr) match {
    case x: StringDefinition => x.value
    case x => x.toString
  }

  def getDecimal(path: Path): BigDecimal = repr(path.pathStr) match {
    case x: DecimalDefinition => x.value
    case x: IntDefinition => x.value
    case x: StringDefinition => x.value.toInt
  }

  def getDefinition(path: Path): AnyDefinition = apply(path)

  def getDataElement(path: Path): DataElement = DataElement(path, apply(path).value)

  def getData(paths: Paths): Data = Data(paths.map(path => DataElement(path, apply(path).value)).toSet)

  def updateDefinition(path: Path, definition: AnyDefinition, consistency: Consistency): Storage = copy(repr.updateDefinition(path.pathStr, definition, consistency))

  def updateDataElement(x: DataElement): Storage = copy(repr.updateValue(x.path.pathStr, x.value))

  def updateData(x: Data): Storage = copy((repr /: x.elements){ case (r, d) => r.updateValue(d.path.pathStr, d.value) })

  def addDefinition(definition: AnyDefinition): Storage = copy(repr.addDefinition(definition))

  def paths: Paths = Paths(repr.impl.keys.toList.map(Path.apply))

  def root: ObjectDefinition = {
    val rootImpl = repr.impl.map({ case (path, reprElem) => s"$$.$path" -> reprElem.withPath(s"$$.$path") })
    val metadata = ObjectMetadata("$", None, "$")
    val root = Repr(rootImpl + ("$" -> metadata))
    root.getObjectDefinition(metadata)
  }

  def prettify: String = s"[${getClass.getSimpleName}]".yellow + root.prettify
}

object Storage {
  def empty: Storage = Storage(Repr.empty)

  def apply(x: Map[PathStr, ReprElement]): Storage = Storage(Repr(x))

  def apply(x: (PathStr, ReprElement)*): Storage = Storage(Repr(x.toMap))
}

case class StorageException(pathStr: PathStr, message: String = "", cause: Option[Exception] = None) extends RuntimeException(message, cause.orNull)
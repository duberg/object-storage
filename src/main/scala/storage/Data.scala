package storage

case class Data(elements: Set[DataElement]) {
  def apply(path: Path): Value = elements
    .find(_.path == path)
    .getOrElse(throw StorageException(path.pathStr, s"Invalid path ${path.pathStr}"))
}

object Data {
  def empty: Data = new Data(Set.empty)

  def apply(x: Map[PathStr, Value]): Data = Data(x.map({
    case (k, v) => DataElement(Path(k), v)
  }).toSet)
}
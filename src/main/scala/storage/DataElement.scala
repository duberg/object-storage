package storage

case class DataElement(path: Path, value: Value)

object DataElement {
  def apply(pathStr: PathStr, value: Value): DataElement = DataElement(Path(pathStr), value)
}

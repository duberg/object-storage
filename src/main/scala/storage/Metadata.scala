package storage

trait Metadata extends ReprElement {
  def value: Value = "metadata"
  def repr: Repr = Repr(path.pathStr -> this)
  def withValue(value: Value): Metadata = this
}

case class ObjectMetadata(name: Name, description: Description, path: Path) extends Metadata {
  def withPath(path: Path) = copy(path = path)
  def withName(x: Name) = copy(name = x)
  def withDescription(description: Description) = copy(description = description)
}

object ObjectMetadata {
  val typeName = "objectMetadata"

  def apply(name: Name, description: Description, path: PathStr): ObjectMetadata = ObjectMetadata(name, description, Path(path))
}

case class ArrayMetadata(name: Name, description: Description, path: Path) extends Metadata {
  def withPath(path: Path) = copy(path = path)
  def withName(x: Name) = copy(name = x)
  def withDescription(description: Description) = copy(description = description)
}

object ArrayMetadata {
  val typeName = "arrayMetadata"

  def apply(name: Name, description: Description, path: PathStr): ArrayMetadata = ArrayMetadata(name, description, Path(path))
}

case class RefMetadata(name: Name, description: Description, ref: Path, path: Path) extends Metadata {
  def withPath(path: Path) = copy(path = path)
  def withRef(ref: Path) = copy(ref = ref)
  def withName(x: Name) = copy(name = x)
  def withDescription(description: Description) = copy(description = description)
}

object RefMetadata {
  val typeName = "refMetadata"

  def apply(name: Name, description: Description, ref: PathStr, path: PathStr): RefMetadata = RefMetadata(name, description, Path(ref), Path(path))
}
package storage

trait Metadata extends ReprElement {
  def name: Name
  def description: Description
  def path: PathStr
  def withValue(value: Value): Metadata = ???
  def withDescription(description: Description): Metadata
  def withPath(path: PathStr): Metadata
}

case class ObjectMetadata(name: Name, description: Description, path: PathStr) extends Metadata {
  def withPath(path: PathStr) = copy(path = path)
  def withDescription(description: Description) = copy(description = description)
}

object ObjectMetadata {
  val typeName = "objectMetadata"
}

case class ArrayMetadata(name: Name, description: Description, path: PathStr) extends Metadata {
  def withPath(path: PathStr) = copy(path = path)
  def withDescription(description: Description) = copy(description = description)
}

object ArrayMetadata {
  val typeName = "arrayMetadata"
}

case class RefMetadata(name: Name, description: Description, ref: PathStr, path: PathStr) extends Metadata {
  def withPath(path: PathStr) = copy(path = path)
  def withRef(ref: PathStr) = copy(ref = ref)
  def withDescription(description: Description) = copy(description = description)
}

object RefMetadata {
  val typeName = "refMetadata"
}
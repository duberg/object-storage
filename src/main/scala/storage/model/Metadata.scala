package storage.model

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

case class CollectionMetadata(name: Name, description: Description, path: PathStr) extends Metadata {
  def withPath(path: PathStr) = copy(path = path)
  def withDescription(description: Description) = copy(description = description)
}
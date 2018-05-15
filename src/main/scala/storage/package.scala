import storage.lang.Assignment

package object storage {
  type Name = Option[String]
  type Description = Option[String]
  type Value = Any
  type PathStr = String
  type AnySimpleElement = SimpleElement[Value]
  type AnyElement = StorageElement[Value]
  type AnyElements = Map[PathStr, AnyElement]
  type Assignments = List[Assignment]
}

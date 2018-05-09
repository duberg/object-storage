package storage

package object model {
  type Name = String
  type Description = Option[String]
  type Value = Any
  type PathStr = String
  type AnySimpleDefinition = SimpleDefinition[Value]
  type AnyDefinition = Definition[Value]

  object PathOpt {

  }
}

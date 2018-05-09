package storage.model

trait Definition[+T] extends Printable { self =>
  def name: Name
  def description: Description
  def value: T
  def path: PathStr
  def repr: Repr
  def withValue(value: Value): AnyDefinition
  def withDescription(description: Description): AnyDefinition
  def withPath(path: PathStr): AnyDefinition
  def isSimple: Boolean =  self match {
    case _: AnySimpleDefinition @unchecked => true
    case _: ComplexDefinition => false
  }
}

abstract class SimpleDefinition[T <: Value] extends Definition[T] with ReprElement { self =>
  def repr: Repr = Repr(path -> self)
  def toPrettyString(depth: Int = 0): String = ((" " * 2) * depth) + "|__".yellow + name.red + " -> " + self.toString
  def prettify: String = toPrettyString()
  def withValue(value: Value): SimpleDefinition[T]
  def withDescription(description: Description): SimpleDefinition[T]
  def withPath(path: PathStr): SimpleDefinition[T]
}

trait ComplexDefinition extends Definition[Map[Name, AnyDefinition]] { self =>
  def toPrettyString(x: Map[Name, AnyDefinition] = value, depth: Int = 0): String = {
    ("" /: x) {
      case (k, (_, d: AnySimpleDefinition)) => k + "\n" + d.toPrettyString(depth)
      case (k, (n, d: ComplexDefinition)) => k + "\n" + ((" " * 2) * depth) + "|__".yellow + n.red + s" [${d.getClass.getSimpleName}]".yellow + toPrettyString(d.value, depth + 1)
    }
  }
  def prettify: String = toPrettyString()
  def withValue(value: Map[Name, AnyDefinition]): ComplexDefinition
  def withDescription(description: Description): ComplexDefinition
  def withDefinition(path: PathStr, definition: AnyDefinition): ComplexDefinition
}

case class BooleanDefinition(name: Name, description: Description, value: Boolean, path: PathStr) extends SimpleDefinition[Boolean] { self =>
  def convert: PartialFunction[Value, Boolean] = {
    case v: Boolean => v
    case v: String => v.toBoolean
    case v => throw StorageException(path, s"$v cannot be converted to boolean")
  }
  def withValue(x: Value) = copy(value = convert(x))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value))
}

case class IntDefinition(name: Name, description: Description, value: Int, path: PathStr) extends SimpleDefinition[Int] { self =>
  def convert: PartialFunction[Value, Int] = {
    case v: Int => v
    case v: BigDecimal => v.toInt
    case v: String => v.toInt
    case v => throw StorageException(path, s"$v cannot be converted to int")
  }
  def withValue(x: Value) = copy(value = convert(x))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value))
}

case class DecimalDefinition(name: Name, description: Description, value: BigDecimal, path: PathStr) extends SimpleDefinition[BigDecimal] { self =>
  def convert: PartialFunction[Value, BigDecimal] = {
    case v: BigDecimal => v
    case v: String => BigDecimal(v)
    case v: Int => BigDecimal(v)
    case v => throw StorageException(path, s"$v cannot be converted to decimal")
  }
  def withValue(x: Value) = copy(value = convert(x))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value))
}

case class StringDefinition(name: Name, description: Description, value: String, path: PathStr) extends SimpleDefinition[String] { self =>
  def withValue(x: Value) = copy(value = x.toString)
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, value.toString)
}

case class ObjectDefinition(name: Name, description: Description, value: Map[Name, AnyDefinition], path: PathStr) extends ComplexDefinition { self =>
  def repr: Repr = {
    val impl: Map[PathStr, ReprElement] = value.values.flatMap(_.repr.impl).toMap
    Repr(impl + (path -> ObjectMetadata(name, description, path)))
  }

  //  def updateValue(reprElem: Value): ObjectDefinition = reprElem match {
//    case v: AnySimpleDefinition =>
//      pathStr.get(v.name) match {
//        case Some(d: AnySimpleDefinition) => copy(
//          description = v.description,
//          pathStr = pathStr + (d.name -> d.updateValue(v.pathStr)))
//        case _ => throw Exception
//      }
//    case v: ObjectDefinition =>
//      copy(
//        description = v.description,
//        pathStr = pathStr.merge(v.pathStr))
//    case _ => throw Exception
//  }
  def withValue(value: Map[Name, AnyDefinition]): ObjectDefinition = ???
  def withValue(value: Value): ObjectDefinition = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr): ObjectDefinition = {
    // !!! name
    copy(
      value = value.mapValues(d => d.withPath(s"$path.${d.path}")),
      path = s"$path.${this.path}"
    )
  }
  def updated(name: Name, description: Description, value: Map[Name, AnyDefinition]) = ???
  def withDefinition(path: PathStr, definition: AnyDefinition) = copy(value = value + (path -> definition))
}

case class CollectionDefinition(name: Name, description: Description, value: Map[Name, AnyDefinition], path: PathStr) extends ComplexDefinition { self =>
  def repr = ???
  def withValue(value: Map[Name, AnyDefinition]): CollectionDefinition = ???
  def withValue(value: Value): ObjectDefinition = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr): CollectionDefinition = {
    copy(value = value.mapValues(d => d.withPath(s"$path.${d.path}")), path = path)
  }
  def updated(name: Name, description: Description, value: Map[Name, AnyDefinition]) = ???
  def withDefinition(path: PathStr, definition: AnyDefinition) = copy(value = value + (path -> definition))
}
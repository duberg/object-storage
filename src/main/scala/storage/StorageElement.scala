package storage

import Implicits._

trait StorageElement[+T] extends Printable { self =>
  def name: Name

  def description: Description

  def value: T

  def repr: Repr

  def path: PathStr

  def withValue(value: Value): AnyElement

  def withDescription(description: Description): AnyElement

  def withPath(path: PathStr): AnyElement

  def isSimple: Boolean = self match {
    case _: AnySimpleElement @unchecked => true
    case _: ComplexElement => false
  }
}

abstract class SimpleElement[+T <: Value] extends StorageElement[T] with ReprElement { self =>
  def repr: Repr = Repr(path -> self)
  def toPrettyString(depth: Int = 0, showIndex: Boolean = false): String = {
    val x = if (showIndex) path.index else path.name
    ((" " * 2) * depth) + "|__".yellow + x.red + " -> " + self.toString
  }
  def prettify: String = toPrettyString()
  def withValue(value: Value): SimpleElement[T]
  def withDescription(description: Description): SimpleElement[T]
  def withPath(path: PathStr): SimpleElement[T]
}

trait ComplexElement extends StorageElement[AnyElements] { self =>
  def toPrettyString(x: List[(PathStr, AnyElement)] = value.toList, depth: Int = 0, showIndex: Boolean = false): String = {
    ("" /: x) {
      case (acc, (_, d: AnySimpleElement)) =>
        acc + "\n" + d.toPrettyString(depth, showIndex)
      case (acc, (n, d: ObjectElement)) =>
        acc + "\n" + ((" " * 2) * depth) + "|__".yellow + n.red +
          s" [${d.getClass.getSimpleName}]".yellow + toPrettyString(d.value.toList, depth + 1)
      case (acc, (n, d: ArrayElement)) =>
        val y = d.value
          .map({
            case (k1, v1) => (v1.path.index, v1)
          })
          .toList
          .sortWith(_._1 < _._1)

        acc + "\n" + ((" " * 2) * depth) + "|__".yellow + n.red +
          s" [${d.getClass.getSimpleName}]".yellow + toPrettyString(y, depth + 1, showIndex = true)
      case (acc, (n, d: Ref)) =>
        acc + "\n" + ((" " * 2) * depth) + "|__".yellow + n.blue +
          s" ->".blue + toPrettyString(List((d.ref, d.value)), depth + 1)
    }
  }
  def prettify: String = toPrettyString()
  def withValue(value: AnyElements): ComplexElement
  def withDescription(description: Description): ComplexElement
  def withElement(path: PathStr, element: AnyElement): ComplexElement
  def addElement(element: AnyElement): ComplexElement
}

case class BooleanElement(name: Name, description: Description, value: Boolean, path: PathStr) extends SimpleElement[Boolean] { self =>
  def withValue(x: Value) = copy(value = convert(x, path))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value, path))
}

object BooleanElement {
  val typeName = "boolean"

  def apply(value: Boolean, path: PathStr): BooleanElement = BooleanElement(None, None, value, path)
}

case class IntElement(name: Name, description: Description, value: Int, path: PathStr) extends SimpleElement[Int] { self =>
  def withValue(x: Value) = copy(value = convert(x, path))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value, path))
}

object IntElement {
  val typeName = "int"

  def apply(value: Int, path: PathStr): IntElement = IntElement(None, None, value, path)
}

case class DecimalElement(name: Name, description: Description, value: BigDecimal, path: PathStr) extends SimpleElement[BigDecimal] { self =>
  def withValue(x: Value) = copy(value = convert(x, path))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value, path))
}

object DecimalElement {
  val typeName = "decimal"

  def apply(value: BigDecimal, path: PathStr): DecimalElement = DecimalElement(None, None, value, path)
}

case class StringElement(name: Name, description: Description, value: String, path: PathStr) extends SimpleElement[String] { self =>
  def withValue(x: Value) = copy(value = x.toString)
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, value.toString)
}

object StringElement {
  val typeName = "string"

  def apply(value: String, path: PathStr): StringElement = StringElement(None, None, value, path)
}

case class ObjectElement(name: Name, description: Description, value: AnyElements, path: PathStr) extends ComplexElement { self =>
  def repr: Repr = {
    val impl: Map[PathStr, ReprElement] = value.values.flatMap(_.repr.impl).toMap
    Repr(impl + (path -> ObjectMetadata(name, description, path)))
  }

  //  def updateValue(reprElem: Value): ObjectElement = reprElem match {
  //    case v: AnySimpleElement =>
  //      pathStr.get(v.name) match {
  //        case Some(d: AnySimpleElement) => copy(
  //          description = v.description,
  //          pathStr = pathStr + (d.name -> d.updateValue(v.pathStr)))
  //        case _ => throw Exception
  //      }
  //    case v: ObjectElement =>
  //      copy(
  //        description = v.description,
  //        pathStr = pathStr.merge(v.pathStr))
  //    case _ => throw Exception
  //  }
  def withValue(value: AnyElements): ObjectElement = ???
  def withValue(value: Value): ObjectElement = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr): ObjectElement = {
    // !!! name
    copy(
      value = value.mapValues(d => d.withPath(s"$path.${d.path}")),
      path = path)
  }
  def updated(name: Name, description: Description, value: AnyElements) = ???
  def withElement(path: PathStr, definition: AnyElement) = copy(value = value + (path -> definition))
  def addElement(definition: AnyElement) = ???
}

object ObjectElement {
  val typeName = "object"

  def apply(value: AnyElements, path: PathStr): ObjectElement = apply(None, None, value, path)
  def apply(x: AnyElement*): ObjectElement = {
    // todo: check paths
    val v = x.map(v => v.path -> v).toMap
    ObjectElement(None, None, v, x.head.path.paths.head)
  }
}

case class ArrayElement(name: Name, description: Description, value: AnyElements, path: PathStr) extends ComplexElement { self =>
  def repr: Repr = {
    val impl: Map[PathStr, ReprElement] = value.values.flatMap(_.repr.impl).toMap
    Repr(impl + (path -> ArrayMetadata(name, description, path)))
  }
  def withValue(value: AnyElements): ArrayElement = ???
  def withValue(value: Value): ObjectElement = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr): ArrayElement = {
    copy(value = value.mapValues(d => d.withPath(s"$path.${d.path}")), path = path)
  }
  def updated(name: Name, description: Description, value: AnyElements) = ???
  def withElement(path: PathStr, definition: AnyElement) = copy(value = value + (path -> definition))
  def addElement(definition: AnyElement) = {
    ???
  }
}

object ArrayElement {
  val typeName = "array"

  def apply(value: AnyElements, path: PathStr): ArrayElement = apply(None, None, value, path)
}

case class Ref(name: Name, description: Description, value: AnyElement, ref: PathStr, path: PathStr) extends StorageElement[AnyElement] {
  def repr = Repr(path -> RefMetadata(name, description, ref, path))

  def toPrettyString(depth: Int = 0, showIndex: Boolean = false): String = {
    val x = if (showIndex) path.index else path.name
    ((" " * 2) * depth) + "|__".blue + x.blue + " -> " + value.prettify
  }
  def prettify: String = toPrettyString()

  def withValue(value: AnyElements) = ???
  def withValue(value: Value) = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: PathStr) = copy(path = path)
  def updated(name: Name, description: Description, value: AnyElements) = ???
  def withElement(path: PathStr, x: AnyElement) = copy(value = x)
  def addElement(definition: AnyElement) = ???
}

object Ref {
  val typeName = "ref"
}
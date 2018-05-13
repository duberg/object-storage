package storage

import Implicits._

trait StorageElement[+T] extends Printable { self =>
  def name: Name
  def description: Description
  def value: T
  def path: Path
  def repr: Repr
  def withValue(value: Value): AnyElement
  def withDescription(description: Description): AnyElement
  def withPath(path: Path): AnyElement
  def isSimple: Boolean = self match {
    case _: AnySimpleElement @unchecked => true
    case _: ComplexElement => false
  }
  def isArrayElement: Boolean = path.isArrayElementPath
  def isRef: Boolean = self match {
    case _: Ref => true
    case _ => false
  }
}

trait SimpleElement[+T] extends StorageElement[T] with ReprElement { self =>
  def repr: Repr = Repr(path.pathStr -> self)
  def toPrettyString(depth: Int = 0): String = {
    val x = if (isArrayElement) "" else path.name.red
    ((" " * 2) * depth) + "|__".yellow + x + " -> " + self.toString
  }
  def prettify: String = toPrettyString()
  def withValue(value: Value): SimpleElement[T]
  def withDescription(description: Description): SimpleElement[T]
  def withPath(path: Path): SimpleElement[T]
}

trait ComplexElement extends StorageElement[AnyElements] { self =>
  def toPrettyString(x: List[(PathStr, AnyElement)] = value.toList, depth: Int = 0): String = {
    ("" /: x) {
      case (acc, (_, d: AnySimpleElement)) =>
        acc + "\n" + d.toPrettyString(depth)
      case (acc, (n, d: ObjectElement)) =>
        acc + "\n" + ((" " * 2) * depth) + "|__".yellow + n.red +
          s" [${d.getClass.getSimpleName}]".yellow + toPrettyString(d.value.toList, depth + 1)
      case (acc, (n, d: ArrayElement)) =>
        val y = d.value
          .map({
            case (k1, v1) => (v1.path.name, v1)
          })
          .toList
          .sortWith(_._1 < _._1)

        acc + "\n" + ((" " * 2) * depth) + "|__".yellow + n.red +
          s" [${d.getClass.getSimpleName}]".yellow + toPrettyString(y, depth + 1)
      case (acc, (n, d: Ref)) =>
        acc + "\n" + ((" " * 2) * depth) + "|__".yellow + n.blue +
          s" ->".blue + toPrettyString(List((d.ref, d.value)), depth + 1)
    }
  }
  def prettify: String = toPrettyString()
  def withValue(value: AnyElements): ComplexElement
  def withDescription(description: Description): ComplexElement
  def createElement(path: Path, element: AnyElement): ComplexElement
  def createElement(element: AnyElement): ComplexElement
}

/**
 * - Разбиваем полный путь в список путей
 * - Группируем по первому элементу списка
 * - Математическая свертка по группам:
 *   - Если группа состоит из одного элемента, значит это простой элемент
 *   - Если группа состоит из больше чем одного элемента значит это объект
 */
class ComplexElementFactory(repr: Repr) extends PathExtractor {
  case class Group(name: PathStr, elements: List[ReprPaths]) {
    def path: Path = Path(name)
    def asAnyElement: AnyElement = elements match {
      // group is simple element
      case ReprPaths(v: AnySimpleElement, _) :: Nil => v.asStorageElement
      // group is reference
      case ReprPaths(v: RefMetadata, _) :: Nil => repr.getReferenceElement(v)
      // group is object element
      case groupElements @ y :: ys =>
        groupElements
          .find(_.pathElements.size == 1)
          .map(_.reprElement)
          .map {
            case x: ObjectMetadata => repr.getObjectElement(x)
            case x: ArrayMetadata => repr.getArrayElement(x)
            case _ => throw StorageException(s"Metadata in group $name not found")
          }
          .get
      case _ => throw StorageException(s"Invalid path in group $name")
    }
  }

  def group(reprPaths: List[ReprPaths]): List[Group] =
    reprPaths
      .groupBy(_.pathElements.head)
      .toList
      .map({ case (name, elements) => Group(name, elements) })

  def buildObject(name: Name, description: Description, path: Path): ObjectElement = {
    val reprPaths = repr.impl
      .filterKeys(_ contains path.withSeparator)
      .map({
        case (p, reprElement) => ReprPaths(
          reprElement = reprElement,
          pathElements = pathElements(p.drop(path.withSeparator.length)))
      })
      .toList

    val obj: ObjectElement = ObjectElement(
      name = name,
      description = description,
      path = path)

    (obj /: group(reprPaths)) { case (element, group) => element.createElement(group.path, group.asAnyElement) }
  }

  def buildArray(name: Name, description: Description, path: Path): ArrayElement = {
    val reprPaths = repr.impl
      .filterKeys(_ contains path.withArrayElementSeparator)
      .map({
        case (p, reprElement) => ReprPaths(
          reprElement = reprElement,
          pathElements = pathElements(p.drop(path.withArrayElementSeparator.length - 1)))
      })
      .toList

    val arr = ArrayElement(
      name = name,
      description = description,
      path = path)

    (arr /: group(reprPaths)) { case (element, group) => element.createElement(group.path, group.asAnyElement) }
  }
}

case class BooleanElement(name: Name, description: Description, value: Boolean, path: Path) extends SimpleElement[Boolean] { self =>
  def withValue(x: Value) = copy(value = convert(x, path))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value, path))
}

object BooleanElement {
  val typeName = "boolean"

  def apply(name: Name, description: Description, value: Boolean, path: PathStr): BooleanElement =
    BooleanElement(name, description, value, Path(path))
  def apply(value: Boolean, path: PathStr): BooleanElement = BooleanElement(None, None, value, Path(path))
}

case class IntElement(name: Name, description: Description, value: Int, path: Path) extends SimpleElement[Int] { self =>
  def withValue(x: Value) = copy(value = convert(x, path))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value, path))
}

object IntElement {
  val typeName = "int"

  def apply(name: Name, description: Description, value: Int, path: PathStr): IntElement =
    IntElement(name, description, value, Path(path))
  def apply(value: Int, path: PathStr): IntElement = IntElement(None, None, value, Path(path))
}

case class DecimalElement(name: Name, description: Description, value: BigDecimal, path: Path) extends SimpleElement[BigDecimal] { self =>
  def withValue(x: Value) = copy(value = convert(x, path))
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, convert(value, path))
}

object DecimalElement {
  val typeName = "decimal"

  def apply(name: Name, description: Description, value: BigDecimal, path: PathStr): DecimalElement =
    DecimalElement(name, description, value, Path(path))
  def apply(value: BigDecimal, path: PathStr): DecimalElement = DecimalElement(None, None, value, Path(path))
}

case class StringElement(name: Name, description: Description, value: String, path: Path) extends SimpleElement[String] { self =>
  def withValue(x: Value) = copy(value = x.toString)
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path) = copy(path = path)
  def updated(name: Name, description: Description, value: Value) = copy(name, description, value.toString)
}

object StringElement {
  val typeName = "string"

  def apply(name: Name, description: Description, value: String, path: PathStr): StringElement =
    StringElement(name, description, value, Path(path))
  def apply(value: String, path: PathStr): StringElement = StringElement(None, None, value, Path(path))
}

case class ObjectElement(name: Name, description: Description, value: AnyElements = Map.empty, path: Path) extends ComplexElement { self =>
  def repr: Repr = {
    val impl: Map[PathStr, ReprElement] = value.values.flatMap(_.repr.impl).toMap
    Repr(impl + (path.pathStr -> ObjectMetadata(name, description, path)))
  }
  def withValue(value: AnyElements): ObjectElement = ???
  def withValue(value: Value): ObjectElement = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path): ObjectElement = {
    copy(
      value = value.mapValues { v =>
        val p = v.path.withParent(path)
        v match {
          case x: Ref => x.withPath(p).withRef(p)
          case _ => v.withPath(p)
        }
      },
      path = path)
  }
  def createElement(path: Path, x: AnyElement): ObjectElement = copy(value = value + (path.pathStr -> x))
  def createElement(x: AnyElement) = ???
}

object ObjectElement {
  val typeName = "object"

  def apply(name: Name, description: Description, value: AnyElements, path: PathStr): ObjectElement =
    ObjectElement(name, description, value, Path(path))

  def apply(value: AnyElements, path: PathStr): ObjectElement = ObjectElement(None, None, value, Path(path))

  def apply(x: AnyElement*): ObjectElement = {
    // todo: check pathElements
    val v = x.map(v => v.path.pathStr -> v).toMap
    ObjectElement(None, None, v, x.head.path.parent)
  }

  def apply(name: Name, description: Description, repr: Repr, path: Path): ObjectElement = {
    new ComplexElementFactory(repr).buildObject(
      name = name,
      description = description,
      path = path)
  }
}

case class ArrayElement(name: Name, description: Description, value: AnyElements = Map.empty, path: Path) extends ComplexElement { self =>
  def repr: Repr = {
    val impl: Map[PathStr, ReprElement] = value.values.flatMap(_.repr.impl).toMap
    Repr(impl + (path.pathStr -> ArrayMetadata(name, description, path)))
  }
  def withValue(value: AnyElements): ArrayElement = ???
  def withValue(value: Value): ObjectElement = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path): ArrayElement = {
    copy(
      value = value.mapValues { v =>
        val p = v.path.withParent(path)
        v match {
          case x: Ref => x.withPath(p).withRef(p)
          case _ => v.withPath(p)
        }
      },
      path = path)
  }
  def createElement(path: Path, x: AnyElement): ArrayElement = copy(value = value + (path.pathStr -> x))
  def createElement(x: AnyElement) = ???
}

object ArrayElement extends PathExtractor {
  val typeName = "array"

  def apply(value: AnyElements, path: PathStr): ArrayElement = apply(None, None, value, Path(path))

  def apply(name: Name, description: Description, repr: Repr, path: Path): ArrayElement = {
    new ComplexElementFactory(repr).buildArray(
      name = name,
      description = description,
      path = path)
  }
}

case class Ref(name: Name, description: Description, value: AnyElement, ref: Path, path: Path) extends StorageElement[AnyElement] {
  def repr = Repr(path.pathStr -> RefMetadata(name, description, ref, path))

  def toPrettyString(depth: Int = 0): String =
    ((" " * 2) * depth) + "|__".blue + path.name.blue + " -> " + value.prettify

  def prettify: String = toPrettyString()

  def withValue(value: AnyElements) = ???
  def withValue(value: Value) = ???
  def withDescription(description: Description) = copy(description = description)
  def withPath(path: Path) = copy(value = value.withPath(path), path = path)
  def withRef(path: Path) = copy(ref = path)
}

object Ref {
  val typeName = "ref"
}
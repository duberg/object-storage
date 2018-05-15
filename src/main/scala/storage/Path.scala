package storage

import storage.Path._
import storage.actor.persistence.PersistenceId

case class Path(elements: List[String], nodeId: Option[PersistenceId] = None) extends PathLike {
  def nodeIdStr = nodeId.get

  def nodeName = nodeId.get.name

  def isArrayElement: Boolean = name match {
    case ArrayElementPattern(a, b) => true
    case _ => false
  }

  def withoutParent: Path = parentOpt match {
    case Some(x) => copy(elements.drop(x.elements.size))
    case None => this
  }

  def withParent(x: Path): Path = copy(
    if (x.isRoot) withoutParent.elements
    else x.elements ::: withoutParent.elements)

  def withSeparator: String = s"$pathStr$Separator"

  def withArrayElementSeparator: String = s"$pathStr$ArrayElementSeparator"

  override def toString = pathStr
  def mkString = s"nodeId: $nodeId, elements: $elements"
}

object Path extends PathExtractor {
  val ArrayElementPattern = "^(.+)(\\[\\d+\\])$".r
  val ArrayElementSeparator = "["
  val Separator = "."

  def apply(path: PathStr): Path = Path(pathElements(path))

  def apply(nodeId: PersistenceId, path: PathStr): Path = Path(
    elements = pathElements(path),
    nodeId = Some(nodeId))

  def apply(nodeId: PersistenceId, elements: List[String]): Path = Path(
    elements = elements,
    nodeId = Some(nodeId))

  def root: Path = Path("$")

  def root(nodeId: PersistenceId): Path = Path(nodeId = nodeId, path = "$")
}

case class Paths(list: List[Path]) {
  def last: Path = list.last
  def listStr: List[PathStr] = list.map(_.pathStr)
  def map[T](f: Path => T): List[T] = list map f
}

object Paths {
  def fromPathStrs(list: List[PathStr]): Paths = Paths(list.map(Path.apply))
}
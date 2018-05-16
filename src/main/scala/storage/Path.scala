package storage

import storage.Path._
import storage.actor.persistence.PersistenceId
import storage.lang.EvaluatorContext

case class Path(elements: List[String]) extends PathLike {
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
}

object Path extends PathExtractor {
  val ArrayElementPattern = "^(.+)(\\[\\d+\\])$".r
  val ArrayElementSeparator = "["
  val Separator = "."

  def apply(path: PathStr): Path = Path(pathElements(path))

  //  def apply(nodeId: PersistenceId, path: PathStr): Path = Path(
  //    elements = pathElements(path),
  //    nodeIdOpt = Some(nodeId))
  //
  //  def apply(nodeId: PersistenceId, elements: List[String]): Path = Path(
  //    elements = elements,
  //    nodeIdOpt = Some(nodeId))

  def root: Path = Path("$")
}

case class NodePath(nodeId: PersistenceId, path: Path) extends PathLike {
  def elements = nodeId.elements ++ path.elements
}

object NodePath {
  def apply(nodePath: NodePathStr): NodePath = {
    val elements = pathElements(nodePath)
    NodePath(
      nodeId = PersistenceId(elements.take(2)),
      path = Path(elements.tail.tail))
  }
  def root(nodeId: PersistenceId): NodePath = NodePath(nodeId, Path.root)
}

trait NodePathMapper extends PathExtractor {
  def resolve(p: NodePathStr)(implicit ctx: EvaluatorContext): NodePath = {
    val elements = pathElements(p)
    if (elements.head contains "$") NodePath(nodeId = ctx.mappings(elements.head), path = Path(elements.tail))
    else NodePath(nodeId = ctx.mappings("$"), path = Path(elements))
  }
}

case class Paths(list: List[Path]) {
  def last: Path = list.last
  def listStr: List[PathStr] = list.map(_.pathStr)
  def map[T](f: Path => T): List[T] = list map f
}

object Paths {
  def fromPathStrs(list: List[PathStr]): Paths = Paths(list.map(Path.apply))
}
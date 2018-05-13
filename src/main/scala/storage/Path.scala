package storage

import java.util.regex.Pattern

import Path._

case class Path(elements: List[String]) {
  lazy val name: String = elements.last
  lazy val pathStr: PathStr = elements.mkString(Separator)
  lazy val parent = parentOpt.getOrElse(throw StorageException("There is no parent object"))
  lazy val parentOpt: Option[Path] = elements match {
    case x :+ _ => Some(Path(x))
    case _ => None
  }

  require(elements.nonEmpty, "Require non empty path")

  def root: Path = Path(elements.head :: Nil)

  def isRoot: Boolean = pathStr == "$"

  def isArrayElementPath: Boolean = name match {
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
}

trait PathExtractor {
  def pathElements(pathStr: PathStr): List[String] = pathStr.split(Pattern.quote(Separator)).toList
}

object Path extends PathExtractor {
  val ArrayElementPattern = "^(.+)(\\[\\d+\\])$".r
  val ArrayElementSeparator = "["
  val Separator = "."

  def apply(path: PathStr): Path = Path(pathElements(path))
  def root: Path = Path("$")
}

case class Paths(list: List[Path]) {
  def last: Path = list.last
  def listStr: List[PathStr] = list.map(_.pathStr)
  def map[T](f: Path => T): List[T] = list map f
}

object Paths {
  def fromPathStrs(list: List[PathStr]): Paths = Paths(list.map(Path.apply))
}
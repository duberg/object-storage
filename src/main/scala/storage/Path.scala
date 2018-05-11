package storage

import storage.Path._

case class Path(pathStr: PathStr) {
  def name: String = paths.last.pathStr
  def paths: Paths = Paths(split(pathStr).map(Path.apply))
  def isRoot: Boolean = pathStr == "$"
  def isArrayElementPath: Boolean = name.matches("^\\[\\d+\\]$")

  require(pathStr.nonEmpty, "Require non empty path")
}

object Path {
  val ArrayElementPattern = "^(.+)(\\[\\d+\\])$".r

  def split(pathStr: PathStr): List[PathStr] = {
    pathStr.split("\\.")
      .toList
      .flatMap {
        case ArrayElementPattern(a, b) => List(a, b)
        case a => List(a)
      }
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
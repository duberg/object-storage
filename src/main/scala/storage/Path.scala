package storage

import storage.Path._

case class Path(pathStr: PathStr) {
  lazy val paths: Paths = Paths(split(pathStr).map(Path.apply))

  lazy val headPathStr: PathStr = pathStr
    .replaceFirst("([^.]+\\.)+", "")
  //.replaceFirst("\\[\\d+\\]", "")

  lazy val index: String = headPathStr
    .replaceFirst("([^\\[])+\\[", "")
    .head
    .toString

  require(pathStr.nonEmpty, "Require non empty path")
}

object Path {
  val ArrayElementPattern = "^(.+)(\\[\\d\\])$".r

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
  lazy val listStr: List[PathStr] = list.map(_.pathStr)

  def map[T](f: Path => T): List[T] = list map f

  //require(list.nonEmpty)
}

object Paths {
  def fromPathStrs(list: List[PathStr]): Paths = Paths(list.map(Path.apply))
}
package storage.model

import Path._

case class Path(pathStr: PathStr) {
  lazy val paths: Paths = Paths(split(pathStr).map(Path.apply))
  lazy val name: Name = pathStr.replaceFirst("([^.]+\\.)+", "")
  lazy val index: String = name.replaceFirst("([^\\[])+\\[", "").head.toString
  lazy val pathWithoutName: Path = ???

  require(pathStr.nonEmpty)
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
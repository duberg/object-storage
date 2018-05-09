package storage.model

case class Path(pathStr: PathStr) {
  lazy val paths: Paths = Paths(pathStr.split("\\.").toList.map(Path.apply))
  lazy val name: Name = pathStr.replaceFirst("([^.]+\\.)+", "")
  lazy val pathWithoutName: Path = ???

  require(pathStr.nonEmpty)
}

case class Paths(list: List[Path]) {
  lazy val listStr: List[PathStr] = list.map(_.pathStr)

  def map[T](f: Path => T): List[T] = list map f

  //require(list.nonEmpty)
}

object Paths {
  def fromPathStrs(list: List[PathStr]): Paths = Paths(list.map(Path.apply))
}
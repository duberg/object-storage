package storage

import storage.Path.Separator

trait PathLike {
  val elements: List[String]

  lazy val name: String = elements.last
  lazy val pathStr: PathStr = elements.mkString(Separator)
  lazy val parent = parentOpt.getOrElse(throw StorageException("There is no parent"))
  lazy val parentOpt: Option[Path] = elements match {
    case x :+ _ => Some(Path(x))
    case _ => None
  }

  require(elements.nonEmpty, "Require non empty path")

  def root: Path = Path(elements.head :: Nil)
  def isRoot: Boolean = pathStr == "$"
}

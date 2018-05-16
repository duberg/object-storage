package storage

import storage.Path.Separator

trait PathLike {
  lazy val name: String = elements.last
  lazy val pathStr: PathStr = elements.mkString(Separator)
  lazy val parent = parentOpt.getOrElse(throw StorageException("There is no parent"))
  lazy val parentOpt: Option[Path] = elements match {
    case x :+ _ => Some(Path(x))
    case _ => None
  }

  require(elements.nonEmpty, "Require non empty path")

  def elements: List[String]
  def head = elements.head
  def tail = elements.tail
  def root: Path = Path(elements.head :: Nil)
  def isRoot: Boolean = pathStr == "$"

  override def toString = pathStr
}

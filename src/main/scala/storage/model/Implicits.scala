package storage.model

object Implicits {
  implicit class PathStrOps(x: PathStr) {
    def paths: List[PathStr] = x.split("\\.").toList
    def name: Name = Path(x).name
  }
}

package storage.model

import Path._

object Implicits {
  implicit class PathStrOps(x: PathStr) {
    def paths: List[PathStr] = split(x)
    def name: Name = Path(x).name
    def index: String = Path(x).index
  }
}

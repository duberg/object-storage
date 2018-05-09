package storage.model

import Path._

object Implicits {
  implicit class PathStrOps(x: PathStr) {
    def paths: List[PathStr] = split(x)
    def headPathStr: PathStr = Path(x).headPathStr
    def index: String = Path(x).index
  }
}

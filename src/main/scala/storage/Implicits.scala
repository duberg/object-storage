package storage

import storage.Path._

object Implicits {
  implicit class PathStrOps(x: PathStr) {
    def paths: List[PathStr] = split(x)
    def headPathStr: PathStr = Path(x).headPathStr
    def index: String = Path(x).index
    def isRoot: Boolean = Path(x).isRoot
  }

  implicit def anyToString(x: Any): String = x.toString

  implicit def anyToInt(x: Any): Int = x match {
    case y: Int => y
    case y: BigDecimal => y.toInt
    case y: String => y.toInt
  }

  implicit def anyToBoolean(x: Any): Boolean = x match {
    case y: Boolean => y
    case y: String => y.toBoolean
  }

  implicit def anyToDecimal(x: Any): BigDecimal = x match {
    case y: BigDecimal => y
    case y: Int => BigDecimal(y)
    case y: String => BigDecimal(y)
  }

  def convert[T](value: Value, path: PathStr)(implicit convert: Value => T): T = convert(value)
}

package storage

object Implicits {
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
  def convert[T](value: Value, path: Path)(implicit convert: Value => T): T = convert(value)
}

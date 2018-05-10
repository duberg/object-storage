package storage

import io.circe._

package object json extends Codec {
  val printer: Printer = Printer.spaces4.copy(dropNullValues = true)

  implicit final class EncoderOps[A](val wrappedEncodeable: A) extends AnyVal {
    def asJson(implicit encoder: Encoder[A]): Json = encoder(wrappedEncodeable)
    def asJsonStr(implicit encoder: Encoder[A]): String = encoder(wrappedEncodeable).pretty(printer)
    def asJsonObject(implicit encoder: ObjectEncoder[A]): JsonObject =
      encoder.encodeObject(wrappedEncodeable)
  }
}

package storage

import io.circe._

package object json extends Codec {
  implicit final class EncoderOps[A](val wrappedEncodeable: A) extends AnyVal {
    def asJson(implicit encoder: Encoder[A]): Json = encoder(wrappedEncodeable)
    def asJsonObject(implicit encoder: ObjectEncoder[A]): JsonObject =
      encoder.encodeObject(wrappedEncodeable)
  }
}

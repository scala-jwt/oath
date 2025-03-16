package io.oath.jsoniter_scala

import com.github.plokhotnyuk.jsoniter_scala.core._
import io.oath.JwtVerifyError
import io.oath.json._

import scala.util.control.Exception.allCatch

object conversion {
  given [P](using codec: JsonValueCodec[P]): ClaimsCodec[P] = new ClaimsCodec[P] {
    override def decode(token: String): Either[JwtVerifyError.DecodingError, P] =
      allCatch
        .withTry(readFromString(token)(codec))
        .toEither
        .left
        .map(error => JwtVerifyError.DecodingError(error.getMessage, error.getCause))

    override def encode(data: P): String =
      writeToString(data)(codec)
  }
}

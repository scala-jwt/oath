package io.oath.jwt.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core._
import io.oath.jwt.ClaimsDecoder
import io.oath.jwt.model.JwtVerifyError

import scala.util.control.Exception.allCatch

trait JsoniterClaimsDecoderConversion {

  implicit def decoderConversion[A](implicit codec: JsonValueCodec[A]): ClaimsDecoder[A] = json =>
    allCatch
      .withTry(readFromString(json))
      .toEither
      .left
      .map(error => JwtVerifyError.DecodingError(error.getMessage, error.getCause))
}

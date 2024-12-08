package io.oath.utils

import io.oath.JwtVerifyError

import java.nio.charset.StandardCharsets
import java.util.Base64 as JBase64
import scala.util.control.Exception.allCatch

private[oath] object Base64 {

  def decodeToken(token: String): Either[JwtVerifyError.DecodingError, String] =
    allCatch
      .withTry(new String(JBase64.getUrlDecoder.decode(token), StandardCharsets.UTF_8))
      .toEither
      .left
      .map(JwtVerifyError.DecodingError("Base64 decode failure.", _))
}

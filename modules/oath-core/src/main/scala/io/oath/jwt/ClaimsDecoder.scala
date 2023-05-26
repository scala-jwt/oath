package io.oath.jwt

import io.oath.jwt.model.JwtVerifyError

trait ClaimsDecoder[T] {
  def decode(token: String): Either[JwtVerifyError.DecodingError, T]
}

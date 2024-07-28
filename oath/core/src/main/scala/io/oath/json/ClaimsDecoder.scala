package io.oath.json

import io.oath.JwtVerifyError

trait ClaimsDecoder[T]:
  def decode(token: String): Either[JwtVerifyError.DecodingError, T]

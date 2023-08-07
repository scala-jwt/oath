package io.oath.json

import io.oath.model.{JwtVerifyError, RegisteredClaims}

trait ClaimsDecoder[T] {
  def decode(token: String): Either[JwtVerifyError.DecodingError, T]
}

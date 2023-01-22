package io.oath.jwt

trait ClaimsEncoder[P] {
  def encode(data: P): String
}

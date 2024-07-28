package io.oath

final case class Jwt[T <: JwtClaims](claims: T, token: String)

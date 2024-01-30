package io.oath

case class Jwt[T <: JwtClaims](claims: T, token: String)

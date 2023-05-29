package io.oath.model

final case class Jwt[+C <: JwtClaims](claims: C, token: String)

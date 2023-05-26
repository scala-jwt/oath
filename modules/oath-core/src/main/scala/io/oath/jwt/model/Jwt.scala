package io.oath.jwt.model

final case class Jwt[+C <: JwtClaims](claims: C, token: String)

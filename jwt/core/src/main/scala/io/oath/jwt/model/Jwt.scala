package io.oath.jwt.model

import eu.timepit.refined.types.string.NonEmptyString

final case class Jwt[+C <: JwtClaims](claims: C, token: NonEmptyString)

package io.oath.syntax

import io.oath.{JwtClaims, RegisteredClaims}

trait RegisteredClaimsOps {
  extension (value: RegisteredClaims) inline def toClaims: JwtClaims.Claims = JwtClaims.Claims(value)
}

object RegisteredClaimsOps extends RegisteredClaimsOps

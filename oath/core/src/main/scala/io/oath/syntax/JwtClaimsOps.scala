package io.oath.syntax

import io.oath.JwtClaims

trait JwtClaimsOps {
  extension [A](value: A)
    def toClaimsP: JwtClaims.ClaimsP[A] = JwtClaims.ClaimsP(value)
    def toClaimsH: JwtClaims.ClaimsH[A] = JwtClaims.ClaimsH(value)

  extension [A, B](value: (A, B)) def toClaimsHP: JwtClaims.ClaimsHP[A, B] = JwtClaims.ClaimsHP(value._1, value._2)
}

object JwtClaimsOps extends JwtClaimsOps

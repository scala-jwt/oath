package io.oath.syntax

import io.oath.JwtClaims

trait JwtClaimsOps {
  extension [A](value: A)
    inline def toClaimsP: JwtClaims.ClaimsP[A] = JwtClaims.ClaimsP(value)
    inline def toClaimsH: JwtClaims.ClaimsH[A] = JwtClaims.ClaimsH(value)

  extension [A, B](value: (A, B))
    inline def toClaimsHP: JwtClaims.ClaimsHP[A, B] = JwtClaims.ClaimsHP(value._1, value._2)
}

object JwtClaimsOps extends JwtClaimsOps

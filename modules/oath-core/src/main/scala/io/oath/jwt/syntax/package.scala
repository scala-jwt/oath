package io.oath.jwt

import io.oath.jwt.model.{JwtClaims, JwtToken, RegisteredClaims}

package object syntax {

  implicit class TokenOps(value: String) {
    def toToken: JwtToken.Token     = JwtToken.Token(value)
    def toTokenH: JwtToken.TokenH   = JwtToken.TokenH(value)
    def toTokenP: JwtToken.TokenP   = JwtToken.TokenP(value)
    def toTokenHP: JwtToken.TokenHP = JwtToken.TokenHP(value)
  }

  implicit class RegisteredClaimsOps(value: RegisteredClaims) {
    def toClaims: JwtClaims.Claims = JwtClaims.Claims(value)
  }

  implicit class SingleValueClaimsOps[A](value: A) {
    def toClaimsP: JwtClaims.ClaimsP[A] = JwtClaims.ClaimsP(value)
    def toClaimsH: JwtClaims.ClaimsH[A] = JwtClaims.ClaimsH(value)
  }

  implicit class TupleValueClaimsOps[A, B](value: (A, B)) {
    def toClaimsHP: JwtClaims.ClaimsHP[A, B] = JwtClaims.ClaimsHP(value._1, value._2)
  }
}

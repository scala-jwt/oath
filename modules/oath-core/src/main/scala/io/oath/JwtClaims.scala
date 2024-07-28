package io.oath

sealed trait JwtClaims:
  val registered: RegisteredClaims

object JwtClaims:
  final case class Claims(registered: RegisteredClaims = RegisteredClaims.empty) extends JwtClaims

  final case class ClaimsH[+H](header: H, registered: RegisteredClaims = RegisteredClaims.empty) extends JwtClaims

  final case class ClaimsP[+P](payload: P, registered: RegisteredClaims = RegisteredClaims.empty) extends JwtClaims

  final case class ClaimsHP[+H, +P](header: H, payload: P, registered: RegisteredClaims = RegisteredClaims.empty)
      extends JwtClaims

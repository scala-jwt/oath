package io.oath

sealed abstract class JwtClaims(val registered: RegisteredClaims)

object JwtClaims {
  final case class Claims(override val registered: RegisteredClaims = RegisteredClaims.empty)
      extends JwtClaims(registered)

  final case class ClaimsH[+H](header: H, override val registered: RegisteredClaims = RegisteredClaims.empty)
      extends JwtClaims(registered)

  final case class ClaimsP[+P](payload: P, override val registered: RegisteredClaims = RegisteredClaims.empty)
      extends JwtClaims(registered)

  final case class ClaimsHP[+H, +P](
      header: H,
      payload: P,
      override val registered: RegisteredClaims = RegisteredClaims.empty,
  ) extends JwtClaims(registered)
}

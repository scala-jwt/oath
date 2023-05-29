package io.oath.model

import java.time.Instant

final case class RegisteredClaims(
    iss: Option[String]  = None,
    sub: Option[String]  = None,
    aud: Seq[String]     = Seq.empty,
    exp: Option[Instant] = None,
    nbf: Option[Instant] = None,
    iat: Option[Instant] = None,
    jti: Option[String]  = None,
)

object RegisteredClaims {

  def empty: RegisteredClaims = RegisteredClaims()
}

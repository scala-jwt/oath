package io.oath.jwt.model

import java.time.Instant

import eu.timepit.refined.types.string.NonEmptyString

final case class RegisteredClaims(
    iss: Option[NonEmptyString] = None,
    sub: Option[NonEmptyString] = None,
    aud: Seq[NonEmptyString] = Seq.empty,
    exp: Option[Instant] = None,
    nbf: Option[Instant] = None,
    iat: Option[Instant] = None,
    jti: Option[NonEmptyString] = None
)

object RegisteredClaims {

  def empty: RegisteredClaims = RegisteredClaims()
}

package io.oath

sealed trait OathToken extends TokenEnumEntry

object OathToken extends TokenEnum[OathToken] {
  case object AccessToken extends OathToken
  case object RefreshToken extends OathToken
  case object ActivationEmailToken extends OathToken
  case object ForgotPasswordToken extends OathToken

  override def values: IndexedSeq[OathToken] = findValues
}

package io.oath

sealed trait OathToken extends OathEnumEntry

object OathToken extends OathEnum[OathToken] {
  case object AccessToken extends OathToken
  case object RefreshToken extends OathToken
  case object ActivationEmailToken extends OathToken
  case object ForgotPasswordToken extends OathToken

  override val tokenValues: Set[OathToken] = findTokenEnumMembers
}

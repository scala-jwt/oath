package io.oath.juror

sealed trait JurorToken extends TokenEnumEntry

object JurorToken extends TokenEnum[JurorToken] {
  case object AccessToken extends JurorToken
  case object RefreshToken extends JurorToken
  case object ActivationEmailToken extends JurorToken
  case object ForgotPasswordToken extends JurorToken

  override def values: IndexedSeq[JurorToken] = findValues
}

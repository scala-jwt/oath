package io.oath

sealed trait TokenSample extends OathEnumEntry

object TokenSample extends OathEnum[TokenSample] {
  case object AccessToken extends TokenSample
  case object refreshToken extends TokenSample

  override val tokenValues: Set[TokenSample] = findTokenEnumMembers
}

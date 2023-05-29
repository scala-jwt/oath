package io.oath

sealed trait TokenSample extends TokenEnumEntry

object TokenSample extends TokenEnum[TokenSample] {
  object AccessToken extends TokenSample

  object refreshToken extends TokenSample

  override def values: IndexedSeq[TokenSample] = findValues
}

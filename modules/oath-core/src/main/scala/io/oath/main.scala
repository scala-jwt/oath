package io.oath

object main extends App {

  sealed trait SomeToken extends TokenEnumEntry

  object SomeToken extends TokenEnum[SomeToken] {
    override def values: IndexedSeq[SomeToken] = findValues

    case object AccessToken extends SomeToken
    case object RefreshToken extends SomeToken


  }
}

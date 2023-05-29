package io.oath.model

sealed trait JwtToken {
  def token: String
}

object JwtToken {

  final case class Token(token: String) extends JwtToken

  final case class TokenH(token: String) extends JwtToken

  final case class TokenP(token: String) extends JwtToken

  final case class TokenHP(token: String) extends JwtToken
}

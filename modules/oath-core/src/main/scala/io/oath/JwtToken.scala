package io.oath

sealed trait JwtToken:
  def token: String

object JwtToken:
  case class Token(token: String) extends JwtToken
  case class TokenH(token: String) extends JwtToken
  case class TokenP(token: String) extends JwtToken
  case class TokenHP(token: String) extends JwtToken

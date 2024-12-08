package io.oath

sealed abstract class JwtToken(val token: String)

object JwtToken {
  final case class Token(override val token: String) extends JwtToken(token)
  final case class TokenH(override val token: String) extends JwtToken(token)
  final case class TokenP(override val token: String) extends JwtToken(token)
  final case class TokenHP(override val token: String) extends JwtToken(token)
}

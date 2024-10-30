package io.oath.syntax

import io.oath.JwtToken

trait JwtTokenOps {
  extension (value: String) {
    def toToken: JwtToken.Token     = JwtToken.Token(value)
    def toTokenH: JwtToken.TokenH   = JwtToken.TokenH(value)
    def toTokenP: JwtToken.TokenP   = JwtToken.TokenP(value)
    def toTokenHP: JwtToken.TokenHP = JwtToken.TokenHP(value)
  }
}

object JwtTokenOps extends JwtTokenOps

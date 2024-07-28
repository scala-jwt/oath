package io.oath.syntax

import io.oath.JwtToken

trait JwtTokenOps {
  extension (value: String) {
    inline def toToken: JwtToken.Token     = JwtToken.Token(value)
    inline def toTokenH: JwtToken.TokenH   = JwtToken.TokenH(value)
    inline def toTokenP: JwtToken.TokenP   = JwtToken.TokenP(value)
    inline def toTokenHP: JwtToken.TokenHP = JwtToken.TokenHP(value)
  }
}

object JwtTokenOps extends JwtTokenOps

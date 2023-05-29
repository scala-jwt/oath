package io

import io.oath.jwt.{JwtIssuer => JIssuer, JwtManager => JManager, JwtVerifier => JVerifier}

package object oath {

  // Type aliases with extra information, useful to determine the token type.
  type JwtIssuer[_]   = JIssuer
  type JwtManager[_]  = JManager
  type JwtVerifier[_] = JVerifier
}

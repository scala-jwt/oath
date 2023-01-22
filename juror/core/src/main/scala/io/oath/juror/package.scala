package io.oath
import io.oath.jwt.{JwtIssuer => JIssuer, JwtManager => JManager, JwtVerifier => JVerifier}

package object juror {

  // Type aliases with extra information, useful to determine the token type.
  type JwtIssuer[_]   = JIssuer
  type JwtManager[_]  = JManager
  type JwtVerifier[_] = JVerifier
}

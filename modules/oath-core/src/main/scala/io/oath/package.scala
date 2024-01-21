package io
import io.oath.utils.*

// Type aliases with extra information, useful to determine the token type.
package oath:
  type JIssuer[_]   = JwtIssuer
  type JManager[_]  = JwtManager
  type JVerifier[_] = JwtVerifier

  inline private def getEnumValues[A]: Set[(A, String)] =
    OathEnumMacro
      .enumValues[A]
      .toSet
      .map(value => value -> convertUpperCamelToLowerHyphen(value.toString))

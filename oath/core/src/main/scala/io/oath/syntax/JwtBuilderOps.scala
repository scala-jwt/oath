package io.oath.syntax

import com.auth0.jwt.JWTCreator.Builder
import io.oath.JwtIssueError
import io.oath.json.ClaimsEncoder

import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.allCatch

private[oath] trait JwtBuilderOps {
  extension (builder: Builder) {
    private def safeEncode[T](
        claims: T,
        toBuilder: String => Builder,
    )(using claimsEncoder: ClaimsEncoder[T]): Either[JwtIssueError.EncodeError, Builder] =
      allCatch
        .withTry(
          claimsEncoder
            .encode(claims)
            .pipe(toBuilder)
        )
        .toEither
        .left
        .map(error => JwtIssueError.EncodeError("Failed when trying to encode token", error))

    def safeEncodeHeader[H](claims: H)(using
        ClaimsEncoder[H]
    ): Either[JwtIssueError.EncodeError, Builder] =
      safeEncode(claims, builder.withHeader)

    def safeEncodePayload[P](claims: P)(using
        ClaimsEncoder[P]
    ): Either[JwtIssueError.EncodeError, Builder] =
      safeEncode(claims, builder.withPayload)
  }
}

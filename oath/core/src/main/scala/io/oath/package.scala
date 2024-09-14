package io.oath

import com.auth0.jwt.JWTCreator.Builder
import com.auth0.jwt.interfaces.DecodedJWT
import io.oath.json.ClaimsEncoder
import io.oath.macros.OathEnumMacro
import io.oath.utils.Formatter

import java.time.Instant
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.allCatch

// TODO: Move to a file and test it properly
inline private def getEnumValues[A]: Set[(A, String)] =
  OathEnumMacro
    .enumValues[A]
    .toSet
    .map(value => value -> Formatter.convertUpperCamelToLowerHyphen(value.toString))

// TODO: Move to file
extension (decodedJWT: DecodedJWT) {
  inline def getOptionIssuer: Option[String]  = Option(decodedJWT.getIssuer)
  inline def getOptionSubject: Option[String] = Option(decodedJWT.getSubject)
  inline def getSeqAudience: Seq[String] =
    Option(decodedJWT.getAudience).map(_.asScala).toSeq.flatten
  inline def getOptionExpiresAt: Option[Instant] = Option(decodedJWT.getExpiresAt).map(_.toInstant)
  inline def getOptionNotBefore: Option[Instant] = Option(decodedJWT.getNotBefore).map(_.toInstant)
  inline def getOptionIssueAt: Option[Instant]   = Option(decodedJWT.getIssuedAt).map(_.toInstant)
  inline def getOptionJwtID: Option[String]      = Option(decodedJWT.getId)
}

// TODO: Move to file
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

  inline def safeEncodeHeader[H](claims: H)(using
      ClaimsEncoder[H]
  ): Either[JwtIssueError.EncodeError, Builder] =
    safeEncode(claims, builder.withHeader)

  inline def safeEncodePayload[P](claims: P)(using
      ClaimsEncoder[P]
  ): Either[JwtIssueError.EncodeError, Builder] =
    safeEncode(claims, builder.withPayload)
}

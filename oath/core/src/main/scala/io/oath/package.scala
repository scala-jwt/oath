package io.oath

import com.auth0.jwt.JWTCreator.Builder
import com.auth0.jwt.interfaces.DecodedJWT
import io.oath.*
import io.oath.json.ClaimsEncoder

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.allCatch

// Type aliases with extra information, useful to determine the token type.
type JIssuer[_]   = JwtIssuer
type JManager[_]  = JwtManager
type JVerifier[_] = JwtVerifier

inline private def getEnumValues[A]: Set[(A, String)] =
  OathEnumMacro
    .enumValues[A]
    .toSet
    .map(value => value -> convertUpperCamelToLowerHyphen(value.toString))

inline private def convertUpperCamelToLowerHyphen(str: String): String =
  str.split("(?=\\p{Lu})").map(_.trim.toLowerCase).filter(_.nonEmpty).mkString("-").trim

inline private def base64DecodeToken(token: String): Either[JwtVerifyError.DecodingError, String] =
  allCatch
    .withTry(new String(Base64.getUrlDecoder.decode(token), StandardCharsets.UTF_8))
    .toEither
    .left
    .map(JwtVerifyError.DecodingError("Base64 decode failure.", _))

// TODO: report bug extension methods declared private in package oath are not visible
extension (decodedJWT: DecodedJWT) {
  inline private def getOptionIssuer: Option[String]  = Option(decodedJWT.getIssuer)
  inline private def getOptionSubject: Option[String] = Option(decodedJWT.getSubject)
  inline private def getSeqAudience: Seq[String] =
    Option(decodedJWT.getAudience).map(_.asScala).toSeq.flatten
  inline private def getOptionExpiresAt: Option[Instant] = Option(decodedJWT.getExpiresAt).map(_.toInstant)
  inline private def getOptionNotBefore: Option[Instant] = Option(decodedJWT.getNotBefore).map(_.toInstant)
  inline private def getOptionIssueAt: Option[Instant]   = Option(decodedJWT.getIssuedAt).map(_.toInstant)
  inline private def getOptionJwtID: Option[String]      = Option(decodedJWT.getId)
}

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

  inline private def safeEncodeHeader[H](claims: H)(using
      ClaimsEncoder[H]
  ): Either[JwtIssueError.EncodeError, Builder] =
    safeEncode(claims, builder.withHeader)

  inline private def safeEncodePayload[P](claims: P)(using
      ClaimsEncoder[P]
  ): Either[JwtIssueError.EncodeError, Builder] =
    safeEncode(claims, builder.withPayload)
}

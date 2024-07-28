package io.oath.utils

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

inline private[utils] val AES: "AES"    = "AES"
inline private[utils] val UTF8: "utf-8" = "utf-8"

private[oath] def convertUpperCamelToLowerHyphen(str: String): String =
  str.split("(?=\\p{Lu})").map(_.trim.toLowerCase).filter(_.nonEmpty).mkString("-").trim

private[oath] def base64DecodeToken(token: String): Either[JwtVerifyError.DecodingError, String] =
  allCatch
    .withTry(new String(Base64.getUrlDecoder.decode(token), StandardCharsets.UTF_8))
    .toEither
    .left
    .map(JwtVerifyError.DecodingError("Base64 decode failure.", _))

// TODO: report bug extension methods declared private in package oath are not visible
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
      .map(error => JwtIssueError.EncodeError(error.getMessage))

  inline private[oath] def safeEncodeHeader[H](claims: H)(using
      ClaimsEncoder[H]
  ): Either[JwtIssueError.EncodeError, Builder] =
    safeEncode(claims, builder.withHeader)

  inline private[oath] def safeEncodePayload[P](claims: P)(using
      ClaimsEncoder[P]
  ): Either[JwtIssueError.EncodeError, Builder] =
    safeEncode(claims, builder.withPayload)
}

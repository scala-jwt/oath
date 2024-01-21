package io.oath

import com.auth0.jwt.JWTCreator.Builder
import com.auth0.jwt.interfaces.DecodedJWT
import io.oath.json.ClaimsEncoder

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.allCatch

package utils:
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

  extension (decodedJWT: DecodedJWT)
    private[oath] def getOptionIssuer: Option[String] =
      Option(decodedJWT.getIssuer)

    private[oath] def getOptionSubject: Option[String] =
      Option(decodedJWT.getSubject)

    private[oath] def getOptionJwtID: Option[String] =
      Option(decodedJWT.getId)

    private[oath] def getSeqAudience: Seq[String] =
      Option(decodedJWT.getAudience).map(_.asScala).toSeq.flatten

    private[oath] def getOptionExpiresAt: Option[Instant] =
      Option(decodedJWT.getExpiresAt).map(_.toInstant)

    private[oath] def getOptionIssueAt: Option[Instant] =
      Option(decodedJWT.getIssuedAt).map(_.toInstant)

    private[oath] def getOptionNotBefore: Option[Instant] =
      Option(decodedJWT.getNotBefore).map(_.toInstant)

  extension (builder: Builder)

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

    private[oath] def safeEncodeHeader[H](claims: H)(using
        ClaimsEncoder[H]
    ): Either[JwtIssueError.EncodeError, Builder] =
      safeEncode(claims, builder.withHeader)

    private[oath] def safeEncodePayload[P](claims: P)(using
        ClaimsEncoder[P]
    ): Either[JwtIssueError.EncodeError, Builder] =
      safeEncode(claims, builder.withPayload)

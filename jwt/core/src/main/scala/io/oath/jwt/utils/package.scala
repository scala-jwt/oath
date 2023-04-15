package io.oath.jwt

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

import com.auth0.jwt.JWTCreator.Builder
import com.auth0.jwt.interfaces.DecodedJWT
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.model.{JwtIssueError, JwtVerifyError}

import scala.util.control.Exception.allCatch

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.chaining.scalaUtilChainingOps

package object utils {

  private[utils] lazy val AES  = "AES"
  private[utils] lazy val UTF8 = "utf-8"

  private[oath] def base64DecodeToken(token: String): Either[JwtVerifyError.DecodingError, String] =
    allCatch
      .withTry(new String(Base64.getUrlDecoder.decode(token), StandardCharsets.UTF_8))
      .toEither
      .left
      .map(JwtVerifyError.DecodingError("Base64 decode failure.", _))

  private[oath] implicit class DecodedJWTOps(private val decodedJWT: DecodedJWT) {
    def getOptionNonEmptyStringIssuer: Option[NonEmptyString] =
      Option(decodedJWT.getIssuer)
        .flatMap(NonEmptyString.unapply)

    def getOptionNonEmptyStringSubject: Option[NonEmptyString] =
      Option(decodedJWT.getSubject)
        .flatMap(NonEmptyString.unapply)

    def getOptionNonEmptyStringID: Option[NonEmptyString] =
      Option(decodedJWT.getId)
        .flatMap(NonEmptyString.unapply)

    def getSeqNonEmptyStringAudience: Seq[NonEmptyString] =
      Option(decodedJWT.getAudience).map(_.asScala).toSeq.flatMap(_.flatMap(NonEmptyString.unapply))

    def getOptionExpiresAt: Option[Instant] =
      Option(decodedJWT.getExpiresAt).map(_.toInstant)

    def getOptionIssueAt: Option[Instant] =
      Option(decodedJWT.getIssuedAt).map(_.toInstant)

    def getOptionNotBefore: Option[Instant] =
      Option(decodedJWT.getNotBefore).map(_.toInstant)
  }

  private[oath] implicit class JWTBuilderOps(private val builder: Builder) {

    private def safeEncode[T](
        claims: T,
        toBuilder: String => Builder
    )(implicit claimsEncoder: ClaimsEncoder[T]): Either[JwtIssueError.EncodeError, Builder] =
      allCatch
        .withTry(
          claimsEncoder
            .encode(claims)
            .pipe(toBuilder)
        )
        .toEither
        .left
        .map(error => JwtIssueError.EncodeError(error.getMessage))

    def safeEncodeHeader[H](claims: H)(implicit
        claimsEncoder: ClaimsEncoder[H]
    ): Either[JwtIssueError.EncodeError, Builder] =
      safeEncode(claims, builder.withHeader)

    def safeEncodePayload[P](claims: P)(implicit
        claimsEncoder: ClaimsEncoder[P]
    ): Either[JwtIssueError.EncodeError, Builder] =
      safeEncode(claims, builder.withPayload)
  }
}

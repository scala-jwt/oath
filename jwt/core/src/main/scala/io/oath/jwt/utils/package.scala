package io.oath.jwt

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

import com.auth0.jwt.interfaces.DecodedJWT
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.model.JwtVerifyError

import scala.util.control.Exception.allCatch

import scala.jdk.CollectionConverters.CollectionHasAsScala

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

}

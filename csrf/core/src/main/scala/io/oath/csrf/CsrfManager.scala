package io.oath.csrf

import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.csrf.config.CsrfManagerConfig
import io.oath.csrf.model.{CsrfParts, CsrfToken}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.util.chaining.scalaUtilChainingOps

final class CsrfManager(config: CsrfManagerConfig) {

  import config._

  private val HMAC_SHA256 = "HmacSHA256"
  private val UTF_8       = "UTF-8"
  private val Separator   = "-"
  private val SplitLimit  = 2

  def issueCSRF(): Option[CsrfToken] =
    NonEmptyString
      .unapply(System.currentTimeMillis().toString)
      .flatMap(message => signBase64Message(message).map(CsrfParts(message, _)))
      .flatMap(encodeToken)

  def verifyCSRF(token: CsrfToken): Boolean =
    decodeToken(token).exists(csrfParts =>
      signBase64Message(csrfParts.message).exists(CsrfUtils.constantTimeEquality(_, csrfParts.signed)))

  private def decodeToken(csrfToken: CsrfToken): Option[CsrfParts] =
    PartialFunction
      .condOpt(csrfToken.token.value.split(Separator, SplitLimit).toList.flatMap(NonEmptyString.unapply)) {
        case message :: signed :: Nil =>
          CsrfParts(message, signed)
      }

  private def encodeToken(csrfParts: CsrfParts): Option[CsrfToken] =
    NonEmptyString
      .unapply(s"${csrfParts.message.value}$Separator${csrfParts.signed.value}")
      .map(CsrfToken)

  private def signBase64Message(message: NonEmptyString): Option[NonEmptyString] =
    Mac
      .getInstance(HMAC_SHA256)
      .tap(_.init(new SecretKeySpec(secret.value.getBytes(UTF_8), HMAC_SHA256)))
      .pipe(_.doFinal(message.value.getBytes(UTF_8)))
      .pipe(CsrfUtils.toBase64)
      .pipe(NonEmptyString.unapply)
}

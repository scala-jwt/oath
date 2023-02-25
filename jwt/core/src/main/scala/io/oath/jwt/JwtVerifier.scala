package io.oath.jwt

import cats.syntax.all._
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions._
import com.auth0.jwt.interfaces.DecodedJWT
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.JwtVerifierConfig
import io.oath.jwt.model.{JwtClaims, JwtToken, JwtVerifyError, RegisteredClaims}
import io.oath.jwt.utils._

import scala.util.control.Exception.allCatch

import scala.util.chaining.scalaUtilChainingOps

final class JwtVerifier(config: JwtVerifierConfig) {

  private lazy val jwtVerifier =
    JWT
      .require(config.algorithm)
      .tap(jwtVerification =>
        config.providedWith.issuerClaim.map(nonEmptyString => jwtVerification.withIssuer(nonEmptyString.value)))
      .tap(jwtVerification =>
        config.providedWith.subjectClaim.map(nonEmptyString => jwtVerification.withSubject(nonEmptyString.value)))
      .tap(jwtVerification =>
        if (config.providedWith.audienceClaims.nonEmpty)
          jwtVerification.withAudience(config.providedWith.audienceClaims.map(_.value).toArray: _*))
      .tap(jwtVerification =>
        config.leewayWindow.leeway.map(duration => jwtVerification.acceptLeeway(duration.toSeconds)))
      .tap(jwtVerification =>
        config.leewayWindow.issuedAt.map(duration => jwtVerification.acceptIssuedAt(duration.toSeconds)))
      .tap(jwtVerification =>
        config.leewayWindow.expiresAt.map(duration => jwtVerification.acceptExpiresAt(duration.toSeconds)))
      .tap(jwtVerification =>
        config.leewayWindow.notBefore.map(duration => jwtVerification.acceptNotBefore(duration.toSeconds)))
      .build()

  private def getRegisteredClaims(decodedJWT: DecodedJWT): RegisteredClaims =
    RegisteredClaims(
      iss = decodedJWT.getOptionNonEmptyStringIssuer,
      sub = decodedJWT.getOptionNonEmptyStringSubject,
      aud = decodedJWT.getSeqNonEmptyStringAudience,
      exp = decodedJWT.getOptionExpiresAt,
      nbf = decodedJWT.getOptionNotBefore,
      iat = decodedJWT.getOptionIssueAt,
      jti = decodedJWT.getOptionNonEmptyStringID
    )

  private def decryptJwt(token: NonEmptyString): Either[JwtVerifyError.DecryptionError, NonEmptyString] =
    config.encrypt
      .map(encryptionConfig => DecryptionUtils.decryptAES(token, encryptionConfig.secret))
      .getOrElse(Right(token))

  private def toNonEmptyString(token: String): Either[JwtVerifyError.VerificationError, NonEmptyString] =
    NonEmptyString.from(token).left.map(error => JwtVerifyError.VerificationError(s"JWT Token error: $error"))

  private def safeDecode[T](
      decodedObject: => Either[JwtVerifyError.DecodingError, T]
  ): Either[JwtVerifyError.DecodingError, T] =
    allCatch
      .withTry(decodedObject)
      .fold(error => JwtVerifyError.DecodingError(error.getMessage, error).asLeft, identity)

  private def handler(decodedJWT: => DecodedJWT): Either[JwtVerifyError, DecodedJWT] =
    allCatch
      .withTry(decodedJWT)
      .toEither
      .left
      .map {
        case e: IllegalArgumentException       => JwtVerifyError.IllegalArgument(e.getMessage)
        case e: AlgorithmMismatchException     => JwtVerifyError.AlgorithmMismatch(e.getMessage)
        case e: SignatureVerificationException => JwtVerifyError.SignatureVerificationError(e.getMessage)
        case e: TokenExpiredException          => JwtVerifyError.TokenExpired(e.getMessage)
        case e: JWTVerificationException       => JwtVerifyError.VerificationError(e.getMessage)
        case e                                 => JwtVerifyError.UnexpectedError(e.getMessage)
      }

  private def verify(token: NonEmptyString): Either[JwtVerifyError, DecodedJWT] =
    handler(
      jwtVerifier
        .verify(token.value)
    )

  def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims] =
    for {
      token          <- toNonEmptyString(jwt.token)
      decryptedToken <- decryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.Claims(registeredClaims)

  def verifyJwt[H](jwt: JwtToken.TokenH)(implicit
      claimsDecoder: ClaimsDecoder[H]
  ): Either[JwtVerifyError, JwtClaims.ClaimsH[H]] =
    for {
      token          <- toNonEmptyString(jwt.token)
      decryptedToken <- decryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      json           <- base64DecodeToken(decodedJwt.getHeader)
      payload        <- safeDecode(claimsDecoder.decode(json))
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.ClaimsH(payload, registeredClaims)

  def verifyJwt[P](jwt: JwtToken.TokenP)(implicit
      claimsDecoder: ClaimsDecoder[P]
  ): Either[JwtVerifyError, JwtClaims.ClaimsP[P]] =
    for {
      token          <- toNonEmptyString(jwt.token)
      decryptedToken <- decryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      json           <- base64DecodeToken(decodedJwt.getPayload)
      payload        <- safeDecode(claimsDecoder.decode(json))
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.ClaimsP(payload, registeredClaims)

  def verifyJwt[H, P](jwt: JwtToken.TokenHP)(implicit
      headerDecoder: ClaimsDecoder[H],
      payloadDecoder: ClaimsDecoder[P]
  ): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]] =
    for {
      token          <- toNonEmptyString(jwt.token)
      decryptedToken <- decryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      jsonHeader     <- base64DecodeToken(decodedJwt.getHeader)
      jsonPayload    <- base64DecodeToken(decodedJwt.getPayload)
      headerClaims   <- safeDecode(headerDecoder.decode(jsonHeader))
      payloadClaims  <- safeDecode(payloadDecoder.decode(jsonPayload))
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.ClaimsHP(headerClaims, payloadClaims, registeredClaims)

}

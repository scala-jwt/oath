package io.oath.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions._
import com.auth0.jwt.interfaces.DecodedJWT
import io.oath.config.JwtVerifierConfig
import io.oath.json.ClaimsDecoder
import io.oath.model._
import io.oath.utils._

import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.allCatch

final class JwtVerifier(config: JwtVerifierConfig) {

  private lazy val jwtVerifier =
    JWT
      .require(config.algorithm)
      .tap(jwtVerification => config.providedWith.issuerClaim.map(str => jwtVerification.withIssuer(str)))
      .tap(jwtVerification => config.providedWith.subjectClaim.map(str => jwtVerification.withSubject(str)))
      .tap(jwtVerification =>
        if (config.providedWith.audienceClaims.nonEmpty)
          jwtVerification.withAudience(config.providedWith.audienceClaims: _*)
      )
      .tap(jwtVerification =>
        config.leewayWindow.leeway.map(duration => jwtVerification.acceptLeeway(duration.toSeconds))
      )
      .tap(jwtVerification =>
        config.leewayWindow.issuedAt.map(duration => jwtVerification.acceptIssuedAt(duration.toSeconds))
      )
      .tap(jwtVerification =>
        config.leewayWindow.expiresAt.map(duration => jwtVerification.acceptExpiresAt(duration.toSeconds))
      )
      .tap(jwtVerification =>
        config.leewayWindow.notBefore.map(duration => jwtVerification.acceptNotBefore(duration.toSeconds))
      )
      .build()

  private def getRegisteredClaims(decodedJWT: DecodedJWT): RegisteredClaims =
    RegisteredClaims(
      iss = decodedJWT.getOptionIssuer,
      sub = decodedJWT.getOptionSubject,
      aud = decodedJWT.getSeqAudience,
      exp = decodedJWT.getOptionExpiresAt,
      nbf = decodedJWT.getOptionNotBefore,
      iat = decodedJWT.getOptionIssueAt,
      jti = decodedJWT.getOptionJwtID,
    )

  private def maybeDecryptJwt(token: String): Either[JwtVerifyError.DecryptionError, String] =
    config.encrypt
      .map(encryptionConfig => DecryptionUtils.decryptAES(token, encryptionConfig.secret))
      .getOrElse(Right(token))

  private def validateToken(token: String): Either[JwtVerifyError.VerificationError, String] =
    Option(token).filter(_.nonEmpty).toRight(JwtVerifyError.VerificationError("JWT Token is empty."))

  private def safeDecode[T](
      decodedObject: => Either[JwtVerifyError.DecodingError, T]
  ): Either[JwtVerifyError.DecodingError, T] =
    allCatch
      .withTry(decodedObject)
      .fold(error => Left(JwtVerifyError.DecodingError(error.getMessage, error)), identity)

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

  private def verify(token: String): Either[JwtVerifyError, DecodedJWT] =
    handler(
      jwtVerifier
        .verify(token)
    )

  def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims] =
    for {
      token          <- validateToken(jwt.token)
      decryptedToken <- maybeDecryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.Claims(registeredClaims)

  def verifyJwt[H](jwt: JwtToken.TokenH)(implicit
      claimsDecoder: ClaimsDecoder[H]
  ): Either[JwtVerifyError, JwtClaims.ClaimsH[H]] =
    for {
      token          <- validateToken(jwt.token)
      decryptedToken <- maybeDecryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      json           <- base64DecodeToken(decodedJwt.getHeader)
      payload        <- safeDecode(claimsDecoder.decode(json))
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.ClaimsH(payload, registeredClaims)

  def verifyJwt[P](jwt: JwtToken.TokenP)(implicit
      claimsDecoder: ClaimsDecoder[P]
  ): Either[JwtVerifyError, JwtClaims.ClaimsP[P]] =
    for {
      token          <- validateToken(jwt.token)
      decryptedToken <- maybeDecryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      json           <- base64DecodeToken(decodedJwt.getPayload)
      payload        <- safeDecode(claimsDecoder.decode(json))
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.ClaimsP(payload, registeredClaims)

  def verifyJwt[H, P](jwt: JwtToken.TokenHP)(implicit
      headerDecoder: ClaimsDecoder[H],
      payloadDecoder: ClaimsDecoder[P],
  ): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]] =
    for {
      token          <- validateToken(jwt.token)
      decryptedToken <- maybeDecryptJwt(token)
      decodedJwt     <- verify(decryptedToken)
      jsonHeader     <- base64DecodeToken(decodedJwt.getHeader)
      jsonPayload    <- base64DecodeToken(decodedJwt.getPayload)
      headerClaims   <- safeDecode(headerDecoder.decode(jsonHeader))
      payloadClaims  <- safeDecode(payloadDecoder.decode(jsonPayload))
      registeredClaims = getRegisteredClaims(decodedJwt)
    } yield JwtClaims.ClaimsHP(headerClaims, payloadClaims, registeredClaims)

}

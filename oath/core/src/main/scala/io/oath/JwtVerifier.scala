package io.oath

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.oath.config.JwtVerifierConfig
import io.oath.json.*
import io.oath.utils.Base64

import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.Exception.allCatch

trait JwtVerifier {
  def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims]
  def verifyJwt[H](jwt: JwtToken.TokenH)(using ClaimsDecoder[H]): Either[JwtVerifyError, JwtClaims.ClaimsH[H]]
  def verifyJwt[P](jwt: JwtToken.TokenP)(using ClaimsDecoder[P]): Either[JwtVerifyError, JwtClaims.ClaimsP[P]]
  def verifyJwt[H, P](
      jwt: JwtToken.TokenHP
  )(using ClaimsDecoder[H], ClaimsDecoder[P]): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]]
}

object JwtVerifier {
  private final class JavaJwtVerifierImpl(config: JwtVerifierConfig) extends JwtVerifier {

    private lazy val jwtVerifier =
      JWT
        .require(config.algorithm)
        .tap(jwtVerification => config.providedWith.issuerClaim.map(str => jwtVerification.withIssuer(str)))
        .tap(jwtVerification => config.providedWith.subjectClaim.map(str => jwtVerification.withSubject(str)))
        .tap(jwtVerification =>
          if (config.providedWith.audienceClaims.nonEmpty)
            jwtVerification.withAudience(config.providedWith.audienceClaims*)
          else ()
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

    private def validateToken(token: String): Either[JwtVerifyError.VerificationError, String] =
      Option(token)
        .filter(_.nonEmpty)
        .toRight(JwtVerifyError.VerificationError("JWTVerifier failed with an empty token."))

    private def safeDecode[T](
        decodedObject: => Either[JwtVerifyError.DecodingError, T]
    ): Either[JwtVerifyError.DecodingError, T] =
      allCatch
        .withTry(decodedObject)
        .fold(error => Left(JwtVerifyError.DecodingError(error.getMessage, error)), identity)

    private def verify(token: String): Either[JwtVerifyError, DecodedJWT] =
      allCatch
        .withTry(jwtVerifier.verify(token))
        .toEither
        .left
        .map(e => JwtVerifyError.VerificationError("JwtVerifier failed with verification error", Some(e)))

    def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims] =
      for
        token      <- validateToken(jwt.token)
        decodedJwt <- verify(token)
        registeredClaims = getRegisteredClaims(decodedJwt)
      yield JwtClaims.Claims(registeredClaims)

    def verifyJwt[H](jwt: JwtToken.TokenH)(using
        claimsDecoder: ClaimsDecoder[H]
    ): Either[JwtVerifyError, JwtClaims.ClaimsH[H]] =
      for
        token      <- validateToken(jwt.token)
        decodedJwt <- verify(token)
        json       <- Base64.decodeToken(decodedJwt.getHeader)
        payload    <- safeDecode(claimsDecoder.decode(json))
        registeredClaims = getRegisteredClaims(decodedJwt)
      yield JwtClaims.ClaimsH(payload, registeredClaims)

    def verifyJwt[P](jwt: JwtToken.TokenP)(using
        claimsDecoder: ClaimsDecoder[P]
    ): Either[JwtVerifyError, JwtClaims.ClaimsP[P]] =
      for
        token      <- validateToken(jwt.token)
        decodedJwt <- verify(token)
        json       <- Base64.decodeToken(decodedJwt.getPayload)
        payload    <- safeDecode(claimsDecoder.decode(json))
        registeredClaims = getRegisteredClaims(decodedJwt)
      yield JwtClaims.ClaimsP(payload, registeredClaims)

    def verifyJwt[H, P](jwt: JwtToken.TokenHP)(using
        headerDecoder: ClaimsDecoder[H],
        payloadDecoder: ClaimsDecoder[P],
    ): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]] =
      for
        token         <- validateToken(jwt.token)
        decodedJwt    <- verify(token)
        jsonHeader    <- Base64.decodeToken(decodedJwt.getHeader)
        jsonPayload   <- Base64.decodeToken(decodedJwt.getPayload)
        headerClaims  <- safeDecode(headerDecoder.decode(jsonHeader))
        payloadClaims <- safeDecode(payloadDecoder.decode(jsonPayload))
        registeredClaims = getRegisteredClaims(decodedJwt)
      yield JwtClaims.ClaimsHP(headerClaims, payloadClaims, registeredClaims)
  }

  def apply(config: JwtVerifierConfig): JwtVerifier = new JavaJwtVerifierImpl(config)
}

package io.oath.jwt

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import java.util.UUID

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.{JWT, JWTCreator}
import io.oath.jwt.config.JwtIssuerConfig
import io.oath.jwt.model.{Jwt, JwtClaims, JwtIssueError, RegisteredClaims}
import io.oath.jwt.utils._

import scala.util.control.Exception.allCatch

import scala.util.chaining.scalaUtilChainingOps

final class JwtIssuer(config: JwtIssuerConfig, clock: Clock = Clock.systemUTC()) {

  private def buildJwt(builder: JWTCreator.Builder, registeredClaims: RegisteredClaims): JWTCreator.Builder =
    builder
      .tap(builder => registeredClaims.iss.map(str => builder.withIssuer(str)))
      .tap(builder => registeredClaims.sub.map(str => builder.withSubject(str)))
      .tap(builder => builder.withAudience(registeredClaims.aud: _*))
      .tap(builder => registeredClaims.jti.map(str => builder.withJWTId(str)))
      .tap(builder => registeredClaims.iat.map(builder.withIssuedAt))
      .tap(builder => registeredClaims.exp.map(builder.withExpiresAt))
      .tap(builder => registeredClaims.nbf.map(builder.withNotBefore))

  private def setRegisteredClaims(adHocRegisteredClaims: RegisteredClaims): RegisteredClaims = {
    val now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS)
    RegisteredClaims(
      iss = adHocRegisteredClaims.iss orElse config.registered.issuerClaim,
      sub = adHocRegisteredClaims.sub orElse config.registered.subjectClaim,
      aud = if (adHocRegisteredClaims.aud.isEmpty) config.registered.audienceClaims else adHocRegisteredClaims.aud,
      exp = adHocRegisteredClaims.exp orElse config.registered.expiresAtOffset.map(duration =>
        now.plusSeconds(duration.toSeconds)),
      nbf = adHocRegisteredClaims.nbf orElse config.registered.notBeforeOffset.map(duration =>
        now.plusSeconds(duration.toSeconds)),
      iat = adHocRegisteredClaims.iat orElse Option.when(config.registered.includeIssueAtClaim)(now),
      jti = adHocRegisteredClaims.jti orElse Option
        .when(config.registered.includeJwtIdClaim)(
          config.registered.issuerClaim
            .map(_ + "-")
            .getOrElse("")
            .pipe(prefix => prefix + UUID.randomUUID().toString))
    )
  }

  private def encryptJwt[T <: JwtClaims](jwt: Jwt[T]): Either[JwtIssueError.EncryptionError, Jwt[T]] =
    config.encrypt
      .map(encryptConfig =>
        EncryptionUtils
          .encryptAES(jwt.token, encryptConfig.secret)
          .map(token => jwt.copy(token = token)))
      .getOrElse(Right(jwt))

  private def safeSign(builder: JWTCreator.Builder, algorithm: Algorithm): Either[JwtIssueError, String] =
    allCatch.withTry(builder.sign(algorithm)).toEither.left.map {
      case e: IllegalArgumentException => JwtIssueError.IllegalArgument(e.getMessage)
      case e: JWTCreationException     => JwtIssueError.JwtCreationIssueError(e.getMessage)
      case e                           => JwtIssueError.UnexpectedIssueError(e.getMessage)
    }

  def issueJwt(
      claims: JwtClaims.Claims = JwtClaims.Claims()
  ): Either[JwtIssueError, Jwt[JwtClaims.Claims]] = {
    val jwtBuilder = JWT.create()
    setRegisteredClaims(claims.registered)
      .pipe(registeredClaims => buildJwt(jwtBuilder, registeredClaims) -> registeredClaims)
      .pipe { case (jwtBuilder, registeredClaims: RegisteredClaims) =>
        safeSign(jwtBuilder, config.algorithm)
          .map(token =>
            Jwt(
              JwtClaims.Claims(registeredClaims),
              token
            ))
      }
      .flatMap(encryptJwt)
  }

  def issueJwt[H](claims: JwtClaims.ClaimsH[H])(implicit
      claimsEncoder: ClaimsEncoder[H]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]] = {
    val jwtBuilder = JWT.create()
    for {
      headerBuilder <- jwtBuilder.safeEncodeHeader(claims.header)
      registeredClaims = setRegisteredClaims(claims.registered)
      builder          = buildJwt(headerBuilder, registeredClaims)
      token <- safeSign(builder, config.algorithm)
      jwt = Jwt(
        claims.copy(registered = registeredClaims),
        token
      )
      encryptedJwt <- encryptJwt(jwt)
    } yield encryptedJwt
  }

  def issueJwt[P](claims: JwtClaims.ClaimsP[P])(implicit
      claimsEncoder: ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]] = {
    val jwtBuilder = JWT.create()
    for {
      payloadBuilder <- jwtBuilder.safeEncodePayload(claims.payload)
      registeredClaims = setRegisteredClaims(claims.registered)
      builder          = buildJwt(payloadBuilder, registeredClaims)
      token <- safeSign(builder, config.algorithm)
      jwt = Jwt(
        claims.copy(registered = registeredClaims),
        token
      )
      encryptedJwt <- encryptJwt(jwt)
    } yield encryptedJwt
  }

  def issueJwt[H, P](claims: JwtClaims.ClaimsHP[H, P])(implicit
      headerClaimsEncoder: ClaimsEncoder[H],
      payloadClaimsEncoder: ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsHP[H, P]]] = {
    val jwtBuilder = JWT.create()
    for {
      payloadBuilder          <- jwtBuilder.safeEncodePayload(claims.payload)
      headerAndPayloadBuilder <- payloadBuilder.safeEncodeHeader(claims.header)
      registeredClaims = setRegisteredClaims(claims.registered)
      builder          = buildJwt(headerAndPayloadBuilder, registeredClaims)
      token <- safeSign(builder, config.algorithm)
      jwt = Jwt(
        claims.copy(registered = registeredClaims),
        token
      )
      encryptedJwt <- encryptJwt(jwt)
    } yield encryptedJwt
  }
}

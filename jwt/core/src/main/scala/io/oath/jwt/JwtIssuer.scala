package io.oath.jwt

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import java.util.UUID

import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.{JWT, JWTCreator}
import com.fasterxml.jackson.databind.ObjectMapper
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.JwtIssuerConfig
import io.oath.jwt.model.{Jwt, JwtClaims, JwtIssueError, RegisteredClaims}
import io.oath.jwt.utils._

import scala.util.control.Exception.allCatch

import scala.util.chaining.scalaUtilChainingOps

final class JwtIssuer(config: JwtIssuerConfig, clock: Clock = Clock.systemUTC()) {

  private lazy val mapper = new ObjectMapper

  private def buildJwt(builder: JWTCreator.Builder, registeredClaims: RegisteredClaims): JWTCreator.Builder =
    builder
      .tap(builder => registeredClaims.iss.map(nonEmptyString => builder.withIssuer(nonEmptyString.value)))
      .tap(builder => registeredClaims.sub.map(nonEmptyString => builder.withSubject(nonEmptyString.value)))
      .tap(builder => builder.withAudience(registeredClaims.aud.map(_.value).toArray: _*))
      .tap(builder => registeredClaims.jti.map(nonEmptyString => builder.withJWTId(nonEmptyString.value)))
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
            .map(_.value + "-")
            .getOrElse("")
            .pipe(prefix => prefix + UUID.randomUUID().toString))
        .flatMap(NonEmptyString.unapply)
    )
  }

  private def safeEncode[H](
      claims: H
  )(implicit claimsEncoder: ClaimsEncoder[H]): Either[JwtIssueError.EncodeError, java.util.Map[String, Object]] =
    allCatch
      .withTry(
        claimsEncoder
          .encode(claims)
          .pipe(json => mapper.readValue(json, classOf[java.util.HashMap[String, Object]]))
      )
      .toEither
      .left
      .map(error => JwtIssueError.EncodeError(error.getMessage))

  private def encryptJwt[T <: JwtClaims](jwt: Jwt[T]): Either[JwtIssueError.EncryptionError, Jwt[T]] =
    config.encrypt
      .map(encryptConfig =>
        EncryptionUtils
          .encryptAES(jwt.token, encryptConfig.secret)
          .map(token => jwt.copy(token = token)))
      .getOrElse(Right(jwt))

  private def handler[T <: JwtClaims](jwt: => Jwt[T]): Either[JwtIssueError, Jwt[T]] =
    allCatch.withTry(jwt).toEither.left.map {
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
        handler(
          Jwt(
            JwtClaims.Claims(registeredClaims),
            NonEmptyString.unsafeFrom(jwtBuilder.sign(config.algorithm))
          )
        )
      }
      .flatMap(encryptJwt)
  }

  def issueJwt[H](claims: JwtClaims.ClaimsH[H])(implicit
      claimsEncoder: ClaimsEncoder[H]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]] = {
    val jwtBuilder = JWT.create()
    for {
      header <- safeEncode(claims.header)
      registeredClaims = setRegisteredClaims(claims.registered)
      builder = jwtBuilder
        .withHeader(header)
        .pipe(builder => buildJwt(builder, registeredClaims))
      jwt <- handler(
        Jwt(
          claims.copy(registered = registeredClaims),
          NonEmptyString.unsafeFrom(builder.sign(config.algorithm))
        )
      )
      encryptedJwt <- encryptJwt(jwt)
    } yield encryptedJwt
  }

  def issueJwt[P](claims: JwtClaims.ClaimsP[P])(implicit
      claimsEncoder: ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]] = {
    val jwtBuilder = JWT.create()
    for {
      payload <- safeEncode(claims.payload)
      registeredClaims = setRegisteredClaims(claims.registered)
      builder = jwtBuilder
        .withPayload(payload)
        .pipe(builder => buildJwt(builder, registeredClaims))
      jwt <- handler(
        Jwt(
          claims.copy(registered = registeredClaims),
          NonEmptyString.unsafeFrom(builder.sign(config.algorithm))
        )
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
      payload <- safeEncode(claims.payload)
      header  <- safeEncode(claims.header)
      registeredClaims = setRegisteredClaims(claims.registered)
      builder = jwtBuilder
        .withPayload(payload)
        .withHeader(header)
        .pipe(builder => buildJwt(builder, registeredClaims))
      jwt <- handler(
        Jwt(
          claims.copy(registered = registeredClaims),
          NonEmptyString.unsafeFrom(builder.sign(config.algorithm))
        )
      )
      encryptedJwt <- encryptJwt(jwt)
    } yield encryptedJwt
  }
}

package io.oath

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.{JWT, JWTCreator}
import io.oath.config.*
import io.oath.json.ClaimsEncoder
import io.oath.syntax.internal.*

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import java.util.UUID
import scala.util.chaining.*
import scala.util.control.Exception.allCatch

trait JwtIssuer {
  def issueJwt(claims: JwtClaims.Claims = JwtClaims.Claims()): Either[JwtIssueError, Jwt[JwtClaims.Claims]]
  def issueJwt[H](claims: JwtClaims.ClaimsH[H])(using
      ClaimsEncoder[H]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]]
  def issueJwt[P](claims: JwtClaims.ClaimsP[P])(using
      ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]]
  def issueJwt[H, P](
      claims: JwtClaims.ClaimsHP[H, P]
  )(using ClaimsEncoder[H], ClaimsEncoder[P]): Either[JwtIssueError, Jwt[JwtClaims.ClaimsHP[H, P]]]
}

object JwtIssuer {

  private final class JavaJwtIssuerImpl(config: JwtIssuerConfig, clock: Clock = Clock.systemUTC()) extends JwtIssuer {

    private def buildJwt(builder: JWTCreator.Builder, registeredClaims: RegisteredClaims): JWTCreator.Builder =
      builder
        .tap(builder => registeredClaims.iss.map(str => builder.withIssuer(str)))
        .tap(builder => registeredClaims.sub.map(str => builder.withSubject(str)))
        .tap(builder => builder.withAudience(registeredClaims.aud*))
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
          now.plusSeconds(duration.toSeconds)
        ),
        nbf = adHocRegisteredClaims.nbf orElse config.registered.notBeforeOffset.map(duration =>
          now.plusSeconds(duration.toSeconds)
        ),
        iat = adHocRegisteredClaims.iat orElse Option.when(config.registered.includeIssueAtClaim)(now),
        jti = adHocRegisteredClaims.jti orElse Option
          .when(config.registered.includeJwtIdClaim)(
            config.registered.issuerClaim
              .map(_ + "-")
              .getOrElse("")
              .pipe(prefix => prefix + UUID.randomUUID().toString)
          ),
      )
    }

    private def safeSign(builder: JWTCreator.Builder, algorithm: Algorithm): Either[JwtIssueError, String] =
      allCatch
        .withTry(builder.sign(algorithm))
        .toEither
        .left
        .map(e => JwtIssueError.SignError("Signing token failed", e))

    def issueJwt(
        claims: JwtClaims.Claims = JwtClaims.Claims()
    ): Either[JwtIssueError, Jwt[JwtClaims.Claims]] = {
      val jwtBuilder = JWT.create()
      setRegisteredClaims(claims.registered)
        .pipe(registeredClaims => buildJwt(jwtBuilder, registeredClaims) -> registeredClaims)
        .pipe { case (jwtBuilder, registeredClaims) =>
          safeSign(jwtBuilder, config.algorithm)
            .map(token =>
              Jwt(
                JwtClaims.Claims(registeredClaims),
                token,
              )
            )
        }
    }

    def issueJwt[H](claims: JwtClaims.ClaimsH[H])(using
        ClaimsEncoder[H]
    ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]] = {
      val jwtBuilder = JWT.create()
      for
        headerBuilder <- jwtBuilder.safeEncodeHeader(claims.header)
        registeredClaims = setRegisteredClaims(claims.registered)
        builder          = buildJwt(headerBuilder, registeredClaims)
        token <- safeSign(builder, config.algorithm)
        jwt = Jwt(
          claims.copy(registered = registeredClaims),
          token,
        )
      yield jwt
    }

    def issueJwt[P](claims: JwtClaims.ClaimsP[P])(using
        ClaimsEncoder[P]
    ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]] = {
      val jwtBuilder = JWT.create()
      for
        payloadBuilder <- jwtBuilder.safeEncodePayload(claims.payload)
        registeredClaims = setRegisteredClaims(claims.registered)
        builder          = buildJwt(payloadBuilder, registeredClaims)
        token <- safeSign(builder, config.algorithm)
        jwt = Jwt(
          claims.copy(registered = registeredClaims),
          token,
        )
      yield jwt
    }

    def issueJwt[H, P](
        claims: JwtClaims.ClaimsHP[H, P]
    )(using ClaimsEncoder[H], ClaimsEncoder[P]): Either[JwtIssueError, Jwt[JwtClaims.ClaimsHP[H, P]]] = {
      val jwtBuilder = JWT.create()
      for
        payloadBuilder          <- jwtBuilder.safeEncodePayload(claims.payload)
        headerAndPayloadBuilder <- payloadBuilder.safeEncodeHeader(claims.header)
        registeredClaims = setRegisteredClaims(claims.registered)
        builder          = buildJwt(headerAndPayloadBuilder, registeredClaims)
        token <- safeSign(builder, config.algorithm)
        jwt = Jwt(
          claims.copy(registered = registeredClaims),
          token,
        )
      yield jwt
    }
  }

  def apply(config: JwtIssuerConfig, clock: Clock = Clock.systemUTC()): JwtIssuer = new JavaJwtIssuerImpl(config, clock)
}
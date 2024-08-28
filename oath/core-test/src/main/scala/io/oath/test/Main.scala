package io.oath.test

import cats.syntax.all.*
import com.auth0.jwt.{JWT, JWTCreator}
import io.oath.config.{EncryptConfig, JwtVerifierConfig}
import io.oath.syntax.all.*
import io.oath.{JwtVerifier, RegisteredClaims}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.util.chaining.scalaUtilChainingOps

object Main extends App, Arbitraries {

  def getInstantNowSeconds: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS)

  def setRegisteredClaims(builder: JWTCreator.Builder, config: JwtVerifierConfig) = {
    val now       = getInstantNowSeconds
    val leeway    = config.leewayWindow.leeway.map(leeway => now.plusSeconds(leeway.toSeconds - 1))
    val expiresAt = config.leewayWindow.expiresAt.map(expiresAt => now.plusSeconds(expiresAt.toSeconds - 1))
    val notBefore = config.leewayWindow.notBefore.map(notBefore => now.plusSeconds(notBefore.toSeconds - 1))
    val issueAt   = config.leewayWindow.issuedAt.map(issueAt => now.plusSeconds(issueAt.toSeconds - 1))

    val registeredClaims = RegisteredClaims(
      config.providedWith.issuerClaim,
      config.providedWith.subjectClaim,
      config.providedWith.audienceClaims,
      expiresAt orElse leeway,
      notBefore orElse leeway,
      issueAt orElse leeway,
      None,
    )

    val builderWithRegistered = builder
      .tap(builder => registeredClaims.iss.map(str => builder.withIssuer(str)))
      .tap(builder => registeredClaims.sub.map(str => builder.withSubject(str)))
      .tap(builder => builder.withAudience(registeredClaims.aud*))
      .tap(builder => registeredClaims.exp.map(builder.withExpiresAt))
      .tap(builder => registeredClaims.nbf.map(builder.withNotBefore))
      .tap(builder => registeredClaims.iat.map(builder.withIssuedAt))

    registeredClaims -> builderWithRegistered
  }

  val defaultConfig = arbJwtVerifierConfig.arbitrary.sample.get

  val jwtVerifier = new JwtVerifier(defaultConfig.copy(encrypt = EncryptConfig("secret").some))

  val (_, builder) = setRegisteredClaims(JWT.create(), defaultConfig)

  val token = builder.sign(defaultConfig.algorithm)

  val verified = jwtVerifier.verifyJwt(token.toToken)

  println(verified)
}

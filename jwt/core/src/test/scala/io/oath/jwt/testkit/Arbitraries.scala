package io.oath.jwt.testkit

import java.time.Instant

import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.NestedHeader.SimpleHeader
import io.oath.jwt.NestedPayload.SimplePayload
import io.oath.jwt.config.EncryptionLoader.EncryptConfig
import io.oath.jwt.config.JwtIssuerConfig.RegisteredConfig
import io.oath.jwt.config.JwtVerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import io.oath.jwt.config.{JwtIssuerConfig, JwtManagerConfig, JwtVerifierConfig}
import io.oath.jwt.model.RegisteredClaims
import io.oath.jwt.{NestedHeader, NestedPayload}
import org.scalacheck.{Arbitrary, Gen}

import scala.concurrent.duration.Duration

import scala.concurrent.duration.DurationInt

trait Arbitraries {

  lazy val genPositiveFiniteDuration        = Gen.posNum[Long].map(Duration.fromNanos)
  lazy val genPositiveFiniteDurationSeconds = Gen.posNum[Int].map(_.seconds)

  implicit lazy val genNonEmptyString = Arbitrary(
    Gen.nonEmptyListOf[Char](Gen.alphaChar).map(_.mkString).map(NonEmptyString.unsafeFrom)
  )
  implicit lazy val arbInstant: Arbitrary[Instant] = Arbitrary(
    Gen.chooseNum(Long.MinValue, Long.MaxValue).map(Instant.ofEpochMilli)
  )

  implicit val jwtIssuerConfigArbitrary: Arbitrary[JwtIssuerConfig] = Arbitrary {
    for {
      encryptKey          <- Gen.option(genNonEmptyString.arbitrary)
      issuerClaim         <- Gen.option(genNonEmptyString.arbitrary)
      subjectClaim        <- Gen.option(genNonEmptyString.arbitrary)
      audienceClaims      <- Gen.listOf(genNonEmptyString.arbitrary)
      includeJwtIdClaim   <- Arbitrary.arbitrary[Boolean]
      includeIssueAtClaim <- Arbitrary.arbitrary[Boolean]
      expiresAtOffset     <- Gen.option(genPositiveFiniteDuration)
      notBeforeOffset     <- Gen.option(genPositiveFiniteDuration)
      registered = RegisteredConfig(issuerClaim,
                                    subjectClaim,
                                    audienceClaims,
                                    includeJwtIdClaim,
                                    includeIssueAtClaim,
                                    expiresAtOffset,
                                    notBeforeOffset)
      encrypt = encryptKey.map(EncryptConfig)
    } yield JwtIssuerConfig(Algorithm.none(), encrypt, registered)
  }

  implicit val jwtVerifierConfigArbitrary: Arbitrary[JwtVerifierConfig] = Arbitrary {
    for {
      encryptKey     <- Gen.option(genNonEmptyString.arbitrary)
      issuerClaim    <- Gen.option(genNonEmptyString.arbitrary)
      subjectClaim   <- Gen.option(genNonEmptyString.arbitrary)
      audienceClaims <- Gen.listOf(genNonEmptyString.arbitrary)
      leeway         <- Gen.option(genPositiveFiniteDurationSeconds)
      issuedAt       <- Gen.option(genPositiveFiniteDurationSeconds)
      expiresAt      <- Gen.option(genPositiveFiniteDurationSeconds)
      notBefore      <- Gen.option(genPositiveFiniteDurationSeconds)
      encrypt      = encryptKey.map(EncryptConfig)
      leewayWindow = LeewayWindowConfig(leeway, issuedAt, expiresAt, notBefore)
      providedWith = ProvidedWithConfig(issuerClaim, subjectClaim, audienceClaims)
    } yield JwtVerifierConfig(Algorithm.none(), encrypt, providedWith, leewayWindow)
  }

  implicit val managerConfigArbitrary: Arbitrary[JwtManagerConfig] = Arbitrary {
    for {
      encryptKey          <- Gen.option(genNonEmptyString.arbitrary)
      issuerClaim         <- Gen.option(genNonEmptyString.arbitrary)
      subjectClaim        <- Gen.option(genNonEmptyString.arbitrary)
      audienceClaims      <- Gen.listOf(genNonEmptyString.arbitrary)
      includeJwtIdClaim   <- Arbitrary.arbitrary[Boolean]
      includeIssueAtClaim <- Arbitrary.arbitrary[Boolean]
      expiresAtOffset     <- Gen.option(genPositiveFiniteDurationSeconds)
      notBeforeOffset     <- Gen.option(genPositiveFiniteDurationSeconds)
      leeway              <- Gen.option(genPositiveFiniteDurationSeconds)
      issuedAt            <- Gen.option(genPositiveFiniteDurationSeconds)
      expiresAt           <- Gen.option(genPositiveFiniteDurationSeconds)
      leewayWindow = LeewayWindowConfig(leeway, issuedAt, expiresAt, notBeforeOffset.map(_.plus(1.second)))
      providedWith = ProvidedWithConfig(issuerClaim, subjectClaim, audienceClaims)
      encrypt      = encryptKey.map(EncryptConfig)
      registered = RegisteredConfig(issuerClaim,
                                    subjectClaim,
                                    audienceClaims,
                                    includeJwtIdClaim,
                                    includeIssueAtClaim,
                                    expiresAtOffset,
                                    notBeforeOffset)
      verifier = JwtVerifierConfig(Algorithm.none(), encrypt, providedWith, leewayWindow)
      issuer   = JwtIssuerConfig(Algorithm.none(), encrypt, registered)
    } yield JwtManagerConfig(issuer, verifier)
  }

  implicit val registeredClaimsArbitrary: Arbitrary[RegisteredClaims] = Arbitrary {
    for {
      iss <- Gen.option(genNonEmptyString.arbitrary)
      sub <- Gen.option(genNonEmptyString.arbitrary)
      aud <- Gen.listOf(genNonEmptyString.arbitrary)
      exp <- Gen.option(arbInstant.arbitrary)
      nbf <- Gen.option(arbInstant.arbitrary)
      iat <- Gen.option(arbInstant.arbitrary)
      jti <- Gen.option(genNonEmptyString.arbitrary)
    } yield RegisteredClaims(iss, sub, aud, exp, nbf, iat, jti)
  }

  implicit val simplePayloadArbitrary: Arbitrary[SimplePayload] = Arbitrary {
    for {
      name <- Gen.alphaStr
      data <- Gen.listOf(Gen.alphaStr)
    } yield SimplePayload(name, data)
  }

  implicit val simpleHeaderArbitrary: Arbitrary[SimpleHeader] = Arbitrary {
    for {
      name <- Gen.alphaStr
      data <- Gen.listOf(Gen.alphaStr)
    } yield SimpleHeader(name, data)
  }

  implicit val nestedPayloadArbitrary: Arbitrary[NestedPayload] = Arbitrary {
    for {
      name    <- Gen.alphaStr
      mapping <- Gen.mapOf(Gen.alphaStr.flatMap(str => simplePayloadArbitrary.arbitrary.map((str, _))))
    } yield NestedPayload(name, mapping)
  }

  implicit val nestedHeaderArbitrary: Arbitrary[NestedHeader] = Arbitrary {
    for {
      name    <- Gen.alphaStr
      mapping <- Gen.mapOf(Gen.alphaStr.flatMap(str => simpleHeaderArbitrary.arbitrary.map((str, _))))
    } yield NestedHeader(name, mapping)
  }
}

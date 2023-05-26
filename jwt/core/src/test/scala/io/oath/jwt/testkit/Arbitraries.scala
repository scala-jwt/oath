package io.oath.jwt.testkit

import com.auth0.jwt.algorithms.Algorithm
import io.oath.jwt.NestedHeader.SimpleHeader
import io.oath.jwt.NestedPayload.SimplePayload
import io.oath.jwt.config.EncryptionLoader.EncryptConfig
import io.oath.jwt.config.JwtIssuerConfig.RegisteredConfig
import io.oath.jwt.config.JwtVerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import io.oath.jwt.config.{JwtIssuerConfig, JwtManagerConfig, JwtVerifierConfig}
import io.oath.jwt.model.RegisteredClaims
import io.oath.jwt.{NestedHeader, NestedPayload}
import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant
import scala.concurrent.duration.Duration

import scala.concurrent.duration.DurationInt

trait Arbitraries {

  lazy val genPositiveFiniteDuration        = Gen.posNum[Long].map(Duration.fromNanos)
  lazy val genPositiveFiniteDurationSeconds = Gen.posNum[Int].map(x => (x + 1).seconds)

  implicit lazy val genNonEmptyString = Arbitrary(
    Gen.nonEmptyListOf[Char](Gen.alphaChar).map(_.mkString)
  )

  implicit val arbInstant: Arbitrary[Instant] = Arbitrary(
    Gen.chooseNum(Long.MinValue, Long.MaxValue).map(Instant.ofEpochMilli)
  )

  implicit val arbEncryptConfig: Arbitrary[EncryptConfig] = Arbitrary {
    genNonEmptyString.arbitrary.map(EncryptConfig)
  }

  implicit val arbJwtIssuerConfig: Arbitrary[JwtIssuerConfig] = Arbitrary {
    for {
      issuerClaim         <- Gen.option(genNonEmptyString.arbitrary)
      subjectClaim        <- Gen.option(genNonEmptyString.arbitrary)
      audienceClaims      <- Gen.listOf(genNonEmptyString.arbitrary)
      includeJwtIdClaim   <- Arbitrary.arbitrary[Boolean]
      includeIssueAtClaim <- Arbitrary.arbitrary[Boolean]
      expiresAtOffset     <- Gen.option(genPositiveFiniteDuration)
      notBeforeOffset     <- Gen.option(genPositiveFiniteDuration)
      registered = RegisteredConfig(
        issuerClaim,
        subjectClaim,
        audienceClaims,
        includeJwtIdClaim,
        includeIssueAtClaim,
        expiresAtOffset,
        notBeforeOffset,
      )
      encrypt <- Gen.option(arbEncryptConfig.arbitrary)
    } yield JwtIssuerConfig(Algorithm.none(), encrypt, registered)
  }

  implicit val arbJwtVerifierConfig: Arbitrary[JwtVerifierConfig] = Arbitrary {
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

  implicit val arbJwtManagerConfig: Arbitrary[JwtManagerConfig] = Arbitrary {
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
      registered = RegisteredConfig(
        issuerClaim,
        subjectClaim,
        audienceClaims,
        includeJwtIdClaim,
        includeIssueAtClaim,
        expiresAtOffset,
        notBeforeOffset,
      )
      verifier = JwtVerifierConfig(Algorithm.none(), encrypt, providedWith, leewayWindow)
      issuer   = JwtIssuerConfig(Algorithm.none(), encrypt, registered)
    } yield JwtManagerConfig(issuer, verifier)
  }

  implicit val arbRegisteredClaims: Arbitrary[RegisteredClaims] = Arbitrary {
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

  implicit val arbSimplePayload: Arbitrary[SimplePayload] = Arbitrary {
    for {
      name <- Gen.alphaStr
      data <- Gen.listOf(Gen.alphaStr)
    } yield SimplePayload(name, data)
  }

  implicit val arbSimpleHeader: Arbitrary[SimpleHeader] = Arbitrary {
    for {
      name <- Gen.alphaStr
      data <- Gen.listOf(Gen.alphaStr)
    } yield SimpleHeader(name, data)
  }

  implicit val arbNestedPayload: Arbitrary[NestedPayload] = Arbitrary {
    for {
      name    <- Gen.alphaStr
      mapping <- Gen.mapOf(Gen.alphaStr.flatMap(str => arbSimplePayload.arbitrary.map((str, _))))
    } yield NestedPayload(name, mapping)
  }

  implicit val arbNestedHeader: Arbitrary[NestedHeader] = Arbitrary {
    for {
      name    <- Gen.alphaStr
      mapping <- Gen.mapOf(Gen.alphaStr.flatMap(str => arbSimpleHeader.arbitrary.map((str, _))))
    } yield NestedHeader(name, mapping)
  }
}

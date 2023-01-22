package io.oath.jwt.config

import com.typesafe.config.{ConfigException, ConfigFactory}
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.EncryptionLoader.EncryptConfig
import io.oath.jwt.testkit.AnyWordSpecBase

import cats.implicits.catsSyntaxOptionId
import scala.concurrent.duration.DurationInt

class JwtVerifierLoaderSpec extends AnyWordSpecBase {

  val configFile                            = "verifier"
  val DefaultTokenConfigLocation            = "default-token"
  val TokenConfigLocation                   = "token"
  val TokenWithEncryptionConfigLocation     = "token-with-encryption"
  val WithoutPublicKeyTokenConfigLocation   = "without-public-key-token"
  val InvalidTokenEmptyStringConfigLocation = "invalid-token-empty-string"
  val InvalidTokenWrongTypeConfigLocation   = "invalid-token-wrong-type"

  "VerifierLoader" should {

    "load default-token verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(DefaultTokenConfigLocation)
      val config       = JwtVerifierConfig.loadOrThrow(configLoader)

      config.encrypt shouldBe empty
      config.providedWith.issuerClaim shouldBe None
      config.providedWith.subjectClaim shouldBe None
      config.providedWith.audienceClaims shouldBe Seq.empty
      config.leewayWindow.leeway shouldBe None
      config.leewayWindow.expiresAt shouldBe None
      config.leewayWindow.issuedAt shouldBe None
      config.leewayWindow.expiresAt shouldBe None
      config.leewayWindow.notBefore shouldBe None
      config.algorithm.getName shouldBe "HS256"
    }

    "load token verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenConfigLocation)
      val config       = JwtVerifierConfig.loadOrThrow(configLoader)

      config.encrypt shouldBe empty
      config.providedWith.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.providedWith.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.providedWith.audienceClaims shouldBe Seq("aud1", "aud2").map(NonEmptyString.unsafeFrom)
      config.leewayWindow.leeway shouldBe 1.minute.some
      config.leewayWindow.issuedAt shouldBe 4.minutes.some
      config.leewayWindow.expiresAt shouldBe 3.minutes.some
      config.leewayWindow.notBefore shouldBe 2.minutes.some
      config.algorithm.getName shouldBe "RS256"
    }

    "load token verifier config values from configuration file with encryption" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenWithEncryptionConfigLocation)
      val config       = JwtVerifierConfig.loadOrThrow(configLoader)

      config.encrypt shouldBe Some(EncryptConfig(NonEmptyString.unsafeFrom("password")))
      config.providedWith.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.providedWith.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.providedWith.audienceClaims shouldBe Seq("aud1", "aud2").map(NonEmptyString.unsafeFrom)
      config.leewayWindow.leeway shouldBe 1.minute.some
      config.leewayWindow.issuedAt shouldBe 4.minutes.some
      config.leewayWindow.expiresAt shouldBe 3.minutes.some
      config.leewayWindow.notBefore shouldBe 2.minutes.some
      config.algorithm.getName shouldBe "RS256"
    }

    "load token verifier config values from reference configuration file using location" in {
      val config = JwtVerifierConfig.loadOrThrow(TokenConfigLocation)

      config.encrypt shouldBe empty
      config.providedWith.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.providedWith.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.providedWith.audienceClaims shouldBe Seq("aud1", "aud2").map(NonEmptyString.unsafeFrom)
      config.leewayWindow.leeway shouldBe 1.minute.some
      config.leewayWindow.issuedAt shouldBe 4.minutes.some
      config.leewayWindow.expiresAt shouldBe 3.minutes.some
      config.leewayWindow.notBefore shouldBe 2.minutes.some
      config.algorithm.getName shouldBe "RS256"
    }

    "fail to load without-public-key-token verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(WithoutPublicKeyTokenConfigLocation)

      the[ConfigException.Missing] thrownBy JwtVerifierConfig.loadOrThrow(configLoader)
    }

    "fail to load invalid-token-empty-string verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(InvalidTokenEmptyStringConfigLocation)

      the[IllegalArgumentException] thrownBy JwtVerifierConfig.loadOrThrow(configLoader)
    }

    "fail to load invalid-token-wrong-type verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(InvalidTokenWrongTypeConfigLocation)

      the[ConfigException.WrongType] thrownBy JwtVerifierConfig.loadOrThrow(configLoader)
    }
  }
}

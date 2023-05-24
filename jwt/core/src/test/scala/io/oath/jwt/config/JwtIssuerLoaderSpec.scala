package io.oath.jwt.config

import com.typesafe.config.{ConfigException, ConfigFactory}
import io.oath.jwt.config.EncryptionLoader.EncryptConfig
import io.oath.jwt.testkit.AnyWordSpecBase

import cats.implicits.catsSyntaxOptionId
import scala.concurrent.duration.DurationInt

class JwtIssuerLoaderSpec extends AnyWordSpecBase {

  val configFile                            = "issuer"
  val DefaultTokenConfigLocation            = "default-token"
  val TokenConfigLocation                   = "token"
  val TokenWithEncryptionConfigLocation     = "token-with-encryption"
  val WithoutPrivateKeyTokenConfigLocation  = "without-private-key-token"
  val InvalidTokenEmptyStringConfigLocation = "invalid-token-empty-string"
  val InvalidTokenWrongTypeConfigLocation   = "invalid-token-wrong-type"

  "IssuerLoader" should {

    "load default-token issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(DefaultTokenConfigLocation)
      val config       = JwtIssuerConfig.loadOrThrow(configLoader)

      config.encrypt shouldBe empty
      config.registered.issuerClaim shouldBe None
      config.registered.subjectClaim shouldBe None
      config.registered.audienceClaims shouldBe Seq.empty
      config.registered.includeIssueAtClaim shouldBe false
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe None
      config.registered.notBeforeOffset shouldBe None
      config.algorithm.getName shouldBe "HS256"
    }

    "load token issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenConfigLocation)
      val config       = JwtIssuerConfig.loadOrThrow(configLoader)

      config.encrypt shouldBe empty
      config.registered.issuerClaim shouldBe "issuer"
      config.registered.subjectClaim shouldBe "subject"
      config.registered.audienceClaims shouldBe Seq("aud1", "aud2")
      config.registered.includeIssueAtClaim shouldBe true
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe 1.day.some
      config.registered.notBeforeOffset shouldBe 1.minute.some
      config.algorithm.getName shouldBe "RS256"
    }

    "load token issuer config values from configuration file with encryption key" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenWithEncryptionConfigLocation)
      val config       = JwtIssuerConfig.loadOrThrow(configLoader)

      config.encrypt shouldBe Some(EncryptConfig("password"))
      config.registered.issuerClaim shouldBe "issuer"
      config.registered.subjectClaim shouldBe "subject"
      config.registered.audienceClaims shouldBe Seq("aud1", "aud2")
      config.registered.includeIssueAtClaim shouldBe true
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe 1.day.some
      config.registered.notBeforeOffset shouldBe 1.minute.some
      config.algorithm.getName shouldBe "RS256"
    }

    "load token issuer config values from reference configuration file using location" in {
      val config = JwtIssuerConfig.loadOrThrow(TokenConfigLocation)

      config.encrypt shouldBe empty
      config.registered.issuerClaim shouldBe "issuer"
      config.registered.subjectClaim shouldBe "subject"
      config.registered.audienceClaims shouldBe Seq("aud1", "aud2")
      config.registered.includeIssueAtClaim shouldBe true
      config.registered.includeJwtIdClaim shouldBe false
      config.registered.expiresAtOffset shouldBe 1.day.some
      config.registered.notBeforeOffset shouldBe 1.minute.some
      config.algorithm.getName shouldBe "RS256"
    }

    "fail to load without-private-key-token issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(WithoutPrivateKeyTokenConfigLocation)

      the[ConfigException.Missing] thrownBy JwtIssuerConfig.loadOrThrow(configLoader)
    }

    "fail to load invalid-token-empty-string issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(InvalidTokenEmptyStringConfigLocation)

      the[IllegalArgumentException] thrownBy JwtIssuerConfig.loadOrThrow(configLoader)
    }

    "failt to load invalid-token-wrong-type issuer config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(InvalidTokenWrongTypeConfigLocation)

      the[ConfigException.BadValue] thrownBy JwtIssuerConfig.loadOrThrow(configLoader)
    }
  }
}

package io.oath.config

import com.typesafe.config.{ConfigException, ConfigFactory}
import io.oath.testkit.*

import scala.concurrent.duration.DurationInt

class JwtVerifierLoaderSpec extends AnyWordSpecBase {

  val configFile                            = "verifier"
  val DefaultTokenConfigLocation            = "default-token"
  val TokenConfigLocation                   = "token"
  val WithoutPublicKeyTokenConfigLocation   = "without-public-key-token"
  val InvalidTokenEmptyStringConfigLocation = "invalid-token-empty-string"
  val InvalidTokenWrongTypeConfigLocation   = "invalid-token-wrong-type"

  "VerifierLoader" should {
    "load default-token verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(DefaultTokenConfigLocation)
      val config       = JwtVerifierConfig.loadOrThrow(configLoader)

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

      config.providedWith.issuerClaim shouldBe Some("issuer")
      config.providedWith.subjectClaim shouldBe Some("subject")
      config.providedWith.audienceClaims shouldBe Seq("aud1", "aud2")
      config.leewayWindow.leeway shouldBe Some(1.minute)
      config.leewayWindow.issuedAt shouldBe Some(4.minutes)
      config.leewayWindow.expiresAt shouldBe Some(3.minutes)
      config.leewayWindow.notBefore shouldBe Some(2.minutes)
      config.algorithm.getName shouldBe "RS256"
    }

    "load token verifier config values from reference.conf file using location" in {
      val config = JwtVerifierConfig.loadOrThrow(TokenConfigLocation)

      config.providedWith.issuerClaim shouldBe Some("issuer")
      config.providedWith.subjectClaim shouldBe Some("subject")
      config.providedWith.audienceClaims shouldBe Seq("aud1", "aud2")
      config.leewayWindow.leeway shouldBe Some(1.minute)
      config.leewayWindow.issuedAt shouldBe Some(4.minutes)
      config.leewayWindow.expiresAt shouldBe Some(3.minutes)
      config.leewayWindow.notBefore shouldBe Some(2.minutes)
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

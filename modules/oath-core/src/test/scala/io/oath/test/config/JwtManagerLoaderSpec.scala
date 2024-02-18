package io.oath.test.config

import com.typesafe.config.ConfigFactory
import io.oath.config.JwtManagerConfig
import io.oath.testkit.AnyWordSpecBase

import scala.concurrent.duration.DurationInt

class JwtManagerLoaderSpec extends AnyWordSpecBase:

  val configFile          = "manager"
  val TokenConfigLocation = "token"

  "ManagerLoader" should:
    "load default-token verifier config values from configuration file" in:
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenConfigLocation)
      val config       = JwtManagerConfig.loadOrThrow(configLoader)

      config.issuer.registered.issuerClaim shouldBe Some("issuer")
      config.issuer.registered.subjectClaim shouldBe Some("subject")
      config.issuer.registered.audienceClaims shouldBe Seq("aud1", "aud2")
      config.issuer.registered.includeIssueAtClaim shouldBe true
      config.issuer.registered.includeJwtIdClaim shouldBe false
      config.issuer.registered.expiresAtOffset shouldBe Some(1.day)
      config.issuer.registered.notBeforeOffset shouldBe Some(1.minute)
      config.issuer.algorithm.getName shouldBe "RS256"

      config.verifier.providedWith.issuerClaim shouldBe Some("issuer")
      config.verifier.providedWith.subjectClaim shouldBe Some("subject")
      config.verifier.providedWith.audienceClaims shouldBe Seq("aud1", "aud2")
      config.verifier.leewayWindow.leeway shouldBe Some(1.minute)
      config.verifier.leewayWindow.issuedAt shouldBe Some(4.minutes)
      config.verifier.leewayWindow.expiresAt shouldBe Some(3.minutes)
      config.verifier.leewayWindow.notBefore shouldBe Some(2.minutes)
      config.verifier.algorithm.getName shouldBe "RS256"

package io.oath.jwt.config

import com.typesafe.config.ConfigFactory
import io.oath.jwt.testkit.AnyWordSpecBase

import cats.implicits.catsSyntaxOptionId
import scala.concurrent.duration.DurationInt

class JwtManagerLoaderSpec extends AnyWordSpecBase {

  val configFile          = "manager"
  val TokenConfigLocation = "token"

  "ManagerLoader" should {
    "load default-token verifier config values from configuration file" in {
      val configLoader = ConfigFactory.load(configFile).getConfig(TokenConfigLocation)
      val config       = JwtManagerConfig.loadOrThrow(configLoader)

      config.issuer.registered.issuerClaim shouldBe Some("issuer")
      config.issuer.registered.subjectClaim shouldBe Some("subject")
      config.issuer.registered.audienceClaims shouldBe Seq("aud1", "aud2")
      config.issuer.registered.includeIssueAtClaim shouldBe true
      config.issuer.registered.includeJwtIdClaim shouldBe false
      config.issuer.registered.expiresAtOffset shouldBe 1.day.some
      config.issuer.registered.notBeforeOffset shouldBe 1.minute.some
      config.issuer.algorithm.getName shouldBe "RS256"

      config.verifier.providedWith.issuerClaim shouldBe Some("issuer")
      config.verifier.providedWith.subjectClaim shouldBe Some("subject")
      config.verifier.providedWith.audienceClaims shouldBe Seq("aud1", "aud2")
      config.verifier.leewayWindow.leeway shouldBe 1.minute.some
      config.verifier.leewayWindow.issuedAt shouldBe 4.minutes.some
      config.verifier.leewayWindow.expiresAt shouldBe 3.minutes.some
      config.verifier.leewayWindow.notBefore shouldBe 2.minutes.some
      config.verifier.algorithm.getName shouldBe "RS256"
    }
  }
}

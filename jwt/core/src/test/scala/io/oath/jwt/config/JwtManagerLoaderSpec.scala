package io.oath.jwt.config

import com.typesafe.config.ConfigFactory
import eu.timepit.refined.types.string.NonEmptyString
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

      config.issuer.registered.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.issuer.registered.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.issuer.registered.audienceClaims shouldBe Seq("aud1", "aud2").map(NonEmptyString.unsafeFrom)
      config.issuer.registered.includeIssueAtClaim shouldBe true
      config.issuer.registered.includeJwtIdClaim shouldBe false
      config.issuer.registered.expiresAtOffset shouldBe 1.day.some
      config.issuer.registered.notBeforeOffset shouldBe 1.minute.some
      config.issuer.algorithm.getName shouldBe "RS256"

      config.verifier.providedWith.issuerClaim shouldBe NonEmptyString.unapply("issuer")
      config.verifier.providedWith.subjectClaim shouldBe NonEmptyString.unapply("subject")
      config.verifier.providedWith.audienceClaims shouldBe Seq("aud1", "aud2").map(NonEmptyString.unsafeFrom)
      config.verifier.leewayWindow.leeway shouldBe 1.minute.some
      config.verifier.leewayWindow.issuedAt shouldBe 4.minutes.some
      config.verifier.leewayWindow.expiresAt shouldBe 3.minutes.some
      config.verifier.leewayWindow.notBefore shouldBe 2.minutes.some
      config.verifier.algorithm.getName shouldBe "RS256"
    }
  }

}

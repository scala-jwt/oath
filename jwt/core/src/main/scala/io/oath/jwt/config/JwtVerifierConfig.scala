package io.oath.jwt.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.EncryptionLoader.EncryptConfig
import io.oath.jwt.config.JwtVerifierConfig._

import scala.concurrent.duration.FiniteDuration

final case class JwtVerifierConfig(algorithm: Algorithm,
                                   encrypt: Option[EncryptConfig],
                                   providedWith: ProvidedWithConfig,
                                   leewayWindow: LeewayWindowConfig
)

object JwtVerifierConfig {

  final case class ProvidedWithConfig(issuerClaim: Option[NonEmptyString] = None,
                                      subjectClaim: Option[NonEmptyString] = None,
                                      audienceClaims: Seq[NonEmptyString] = Seq.empty
  )

  final case class LeewayWindowConfig(leeway: Option[FiniteDuration] = None,
                                      issuedAt: Option[FiniteDuration] = None,
                                      expiresAt: Option[FiniteDuration] = None,
                                      notBefore: Option[FiniteDuration] = None
  )

  private val VerifierConfigLocation     = "verifier"
  private val AlgorithmConfigLocation    = "algorithm"
  private val EncryptConfigLocation      = "encrypt"
  private val ProvidedWithConfigLocation = "provided-with"
  private val LeewayWindowConfigLocation = "leeway-window"

  private def loadOrdThrowProvidedWithConfig(providedWithScoped: Config): ProvidedWithConfig = {
    val issuerClaim   = providedWithScoped.getMaybeNonEmptyString("issuer-claim")
    val subjectClaim  = providedWithScoped.getMaybeNonEmptyString("subject-claim")
    val audienceClaim = providedWithScoped.getSeqNonEmptyString("audience-claims")
    ProvidedWithConfig(issuerClaim, subjectClaim, audienceClaim)
  }

  private def loadOrThrowLeewayWindowConfig(leewayWindowScoped: Config): LeewayWindowConfig = {
    val leeway    = leewayWindowScoped.getMaybeFiniteDuration("leeway")
    val issuedAt  = leewayWindowScoped.getMaybeFiniteDuration("issued-at")
    val expiresAt = leewayWindowScoped.getMaybeFiniteDuration("expires-at")
    val notBefore = leewayWindowScoped.getMaybeFiniteDuration("not-before")
    LeewayWindowConfig(leeway, issuedAt, expiresAt, notBefore)
  }

  def loadOrThrow(config: Config): JwtVerifierConfig = {
    val maybeVerificationScoped = config.getMaybeConfig(VerifierConfigLocation)
    val algorithm = AlgorithmLoader.loadOrThrow(config.getConfig(AlgorithmConfigLocation), forIssuing = false)
    val encrypt   = config.getMaybeConfig(EncryptConfigLocation).map(EncryptionLoader.loadOrThrow)
    val providedWith =
      for {
        verificationScoped <- maybeVerificationScoped
        providedWithScoped <- verificationScoped.getMaybeConfig(ProvidedWithConfigLocation)
      } yield loadOrdThrowProvidedWithConfig(providedWithScoped)

    val leewayWindow =
      for {
        verificationScoped <- maybeVerificationScoped
        leewayWindowScoped <- verificationScoped.getMaybeConfig(LeewayWindowConfigLocation)
      } yield loadOrThrowLeewayWindowConfig(leewayWindowScoped)

    JwtVerifierConfig(algorithm,
                      encrypt,
                      providedWith.getOrElse(ProvidedWithConfig()),
                      leewayWindow.getOrElse(LeewayWindowConfig()))
  }

  def loadOrThrow(location: String): JwtVerifierConfig = {
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
  }
}

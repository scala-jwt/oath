package io.oath.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.{Config, ConfigFactory}
import io.oath.config.EncryptConfig.*
import io.oath.config.JwtVerifierConfig.*

import scala.concurrent.duration.FiniteDuration

final case class JwtVerifierConfig(
    algorithm: Algorithm,
    encrypt: Option[EncryptConfig],
    providedWith: ProvidedWithConfig,
    leewayWindow: LeewayWindowConfig,
)

object JwtVerifierConfig {
  inline private val VerifierConfigLocation     = "verifier"
  inline private val AlgorithmConfigLocation    = "algorithm"
  inline private val EncryptConfigLocation      = "encrypt"
  inline private val ProvidedWithConfigLocation = "provided-with"
  inline private val LeewayWindowConfigLocation = "leeway-window"

  final case class ProvidedWithConfig(
      issuerClaim: Option[String]  = None,
      subjectClaim: Option[String] = None,
      audienceClaims: Seq[String]  = Seq.empty,
  )

  final case class LeewayWindowConfig(
      leeway: Option[FiniteDuration]    = None,
      issuedAt: Option[FiniteDuration]  = None,
      expiresAt: Option[FiniteDuration] = None,
      notBefore: Option[FiniteDuration] = None,
  )

  private def loadOrdThrowProvidedWithConfig(providedWithScoped: Config): ProvidedWithConfig =
    val issuerClaim   = providedWithScoped.getMaybeNonEmptyString("issuer-claim")
    val subjectClaim  = providedWithScoped.getMaybeNonEmptyString("subject-claim")
    val audienceClaim = providedWithScoped.getSeqNonEmptyString("audience-claims")
    ProvidedWithConfig(issuerClaim, subjectClaim, audienceClaim)

  private def loadOrThrowLeewayWindowConfig(leewayWindowScoped: Config): LeewayWindowConfig =
    val leeway    = leewayWindowScoped.getMaybeFiniteDuration("leeway")
    val issuedAt  = leewayWindowScoped.getMaybeFiniteDuration("issued-at")
    val expiresAt = leewayWindowScoped.getMaybeFiniteDuration("expires-at")
    val notBefore = leewayWindowScoped.getMaybeFiniteDuration("not-before")
    LeewayWindowConfig(leeway, issuedAt, expiresAt, notBefore)

  private[oath] def loadOrThrowOath(location: String): JwtVerifierConfig =
    JwtVerifierConfig.loadOrThrow(rootConfig.getConfig(location))

  def none(): JwtVerifierConfig = JwtVerifierConfig(Algorithm.none(), None, ProvidedWithConfig(), LeewayWindowConfig())

  def loadOrThrow(config: Config): JwtVerifierConfig =
    (for
      algorithmScoped <- config.getMaybeConfig(AlgorithmConfigLocation)
      algorithmConfig         = AlgorithmLoader.loadOrThrow(algorithmScoped, isIssuer = false)
      maybeEncryptionScoped   = config.getMaybeConfig(EncryptConfigLocation)
      maybeEncryptConfig      = maybeEncryptionScoped.map(EncryptConfig.loadOrThrow)
      maybeVerificationScoped = config.getMaybeConfig(VerifierConfigLocation)
      maybeProvidedWithConfig =
        for
          verificationScoped <- maybeVerificationScoped
          providedWithScoped <- verificationScoped.getMaybeConfig(ProvidedWithConfigLocation)
        yield loadOrdThrowProvidedWithConfig(providedWithScoped)
      maybeLeewayWindowConfig =
        for
          verificationScoped <- maybeVerificationScoped
          leewayWindowScoped <- verificationScoped.getMaybeConfig(LeewayWindowConfigLocation)
        yield loadOrThrowLeewayWindowConfig(leewayWindowScoped)
    yield JwtVerifierConfig(
      algorithmConfig,
      maybeEncryptConfig,
      maybeProvidedWithConfig.getOrElse(ProvidedWithConfig()),
      maybeLeewayWindowConfig.getOrElse(LeewayWindowConfig()),
    )).getOrElse(none())

  def loadOrThrow(location: String): JwtVerifierConfig =
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
}

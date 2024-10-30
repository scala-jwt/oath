package io.oath.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.{Config, ConfigFactory}
import io.oath.config.JwtVerifierConfig.*

import scala.concurrent.duration.FiniteDuration

final case class JwtVerifierConfig(
    algorithm: Algorithm,
    providedWith: ProvidedWithConfig,
    leewayWindow: LeewayWindowConfig,
)

object JwtVerifierConfig {
  private val VerifierConfigLocation     = "verifier"
  private val AlgorithmConfigLocation    = "algorithm"
  private val ProvidedWithConfigLocation = "provided-with"
  private val LeewayWindowConfigLocation = "leeway-window"

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

  def none(): JwtVerifierConfig = JwtVerifierConfig(Algorithm.none(), ProvidedWithConfig(), LeewayWindowConfig())

  def loadOrThrow(config: Config): JwtVerifierConfig =
    (for
      algorithmScoped <- config.getMaybeConfig(AlgorithmConfigLocation)
      algorithmConfig         = AlgorithmLoader.loadOrThrow(algorithmScoped, isIssuer = false)
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
      maybeProvidedWithConfig.getOrElse(ProvidedWithConfig()),
      maybeLeewayWindowConfig.getOrElse(LeewayWindowConfig()),
    )).getOrElse(none())

  def loadOrThrow(location: String): JwtVerifierConfig =
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
}

package io.oath.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.{Config, ConfigFactory}
import io.oath.config.JwtIssuerConfig.RegisteredConfig

import scala.concurrent.duration.FiniteDuration

final case class JwtIssuerConfig(algorithm: Algorithm, registered: RegisteredConfig)

object JwtIssuerConfig {
  inline private val IssuerConfigLocation     = "issuer"
  inline private val AlgorithmConfigLocation  = "algorithm"
  inline private val RegisteredConfigLocation = "registered"

  final case class RegisteredConfig(
      issuerClaim: Option[String]             = None,
      subjectClaim: Option[String]            = None,
      audienceClaims: Seq[String]             = Seq.empty,
      includeJwtIdClaim: Boolean              = false,
      includeIssueAtClaim: Boolean            = false,
      expiresAtOffset: Option[FiniteDuration] = None,
      notBeforeOffset: Option[FiniteDuration] = None,
  )

  private def loadOrThrowRegisteredConfig(registeredScoped: Config): RegisteredConfig =
    val issuerClaim          = registeredScoped.getMaybeNonEmptyString("issuer-claim")
    val subjectClaim         = registeredScoped.getMaybeNonEmptyString("subject-claim")
    val audienceClaim        = registeredScoped.getSeqNonEmptyString("audience-claims")
    val includeIssuedAtClaim = registeredScoped.getBooleanDefaultFalse("include-issued-at-claim")
    val includeJwtIdClaim    = registeredScoped.getBooleanDefaultFalse("include-jwt-id-claim")
    val expiresAtOffset      = registeredScoped.getMaybeFiniteDuration("expires-at-offset")
    val notBeforeOffset      = registeredScoped.getMaybeFiniteDuration("not-before-offset")

    RegisteredConfig(
      issuerClaim,
      subjectClaim,
      audienceClaim,
      includeJwtIdClaim,
      includeIssuedAtClaim,
      expiresAtOffset,
      notBeforeOffset,
    )

  def none(): JwtIssuerConfig = JwtIssuerConfig(Algorithm.none(), RegisteredConfig())

  def loadOrThrow(config: Config): JwtIssuerConfig =
    (for
      algorithmScoped <- config.getMaybeConfig(AlgorithmConfigLocation)
      algorithmConfig = AlgorithmLoader.loadOrThrow(algorithmScoped, isIssuer = true)
      maybeRegisteredConfig =
        for
          issuerScoped     <- config.getMaybeConfig(IssuerConfigLocation)
          registeredScoped <- issuerScoped.getMaybeConfig(RegisteredConfigLocation)
        yield loadOrThrowRegisteredConfig(registeredScoped)
    yield JwtIssuerConfig(algorithmConfig, maybeRegisteredConfig.getOrElse(RegisteredConfig())))
      .getOrElse(none())

  def loadOrThrow(location: String): JwtIssuerConfig =
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)

  private[oath] def loadOrThrowOath(location: String): JwtIssuerConfig =
    JwtIssuerConfig.loadOrThrow(rootConfig.getConfig(location))

}

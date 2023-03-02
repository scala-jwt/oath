package io.oath.jwt.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.EncryptionLoader.EncryptConfig

import scala.concurrent.duration.FiniteDuration

import JwtIssuerConfig._

final case class JwtIssuerConfig(algorithm: Algorithm, encrypt: Option[EncryptConfig], registered: RegisteredConfig)

object JwtIssuerConfig {

  final case class RegisteredConfig(issuerClaim: Option[NonEmptyString] = None,
                                    subjectClaim: Option[NonEmptyString] = None,
                                    audienceClaims: Seq[NonEmptyString] = Seq.empty,
                                    includeJwtIdClaim: Boolean = false,
                                    includeIssueAtClaim: Boolean = false,
                                    expiresAtOffset: Option[FiniteDuration] = None,
                                    notBeforeOffset: Option[FiniteDuration] = None
  )

  private val IssuerConfigLocation     = "issuer"
  private val AlgorithmConfigLocation  = "algorithm"
  private val EncryptConfigLocation    = "encrypt"
  private val RegisteredConfigLocation = "registered"

  private def loadOrThrowRegisteredConfig(registeredScoped: Config): RegisteredConfig = {
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
      notBeforeOffset
    )
  }

  def none(): JwtIssuerConfig = JwtIssuerConfig(Algorithm.none(), None, RegisteredConfig())

  def loadOrThrow(config: Config): JwtIssuerConfig = {
    val algorithm         = AlgorithmLoader.loadOrThrow(config.getConfig(AlgorithmConfigLocation), forIssuing = true)
    val maybeIssuerScoped = config.getMaybeConfig(IssuerConfigLocation)
    val encrypt           = config.getMaybeConfig(EncryptConfigLocation).map(EncryptionLoader.loadOrThrow)
    val registered = maybeIssuerScoped
      .map(scoped => loadOrThrowRegisteredConfig(scoped.getConfig(RegisteredConfigLocation)))
      .getOrElse(RegisteredConfig())

    JwtIssuerConfig(algorithm, encrypt, registered)
  }

  def loadOrThrow(location: String): JwtIssuerConfig = {
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
  }
}

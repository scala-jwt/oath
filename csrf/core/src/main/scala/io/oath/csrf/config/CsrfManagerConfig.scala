package io.oath.csrf.config

import com.typesafe.config.{Config, ConfigFactory}
import eu.timepit.refined.types.string.NonEmptyString

import scala.util.chaining.scalaUtilChainingOps

final case class CsrfManagerConfig(secret: NonEmptyString)

object CsrfManagerConfig {
  private val CsrfConfigLocation   = "csrf"
  private val SecretKeyConfigValue = "secret"

  def loadOrThrow(config: Config): CsrfManagerConfig =
    config
      .getConfig(CsrfConfigLocation)
      .getString(SecretKeyConfigValue)
      .pipe(NonEmptyString.unsafeFrom)
      .pipe(CsrfManagerConfig(_))

  def loadOrThrow(location: String): CsrfManagerConfig = {
    val configLocation = ConfigFactory.load().getConfig(location)
    loadOrThrow(configLocation)
  }
}

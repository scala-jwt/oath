package io.oath.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.chaining.scalaUtilChainingOps

final case class JwtManagerConfig(issuer: JwtIssuerConfig, verifier: JwtVerifierConfig)

object JwtManagerConfig {

  private[oath] def loadOrThrowOath(location: String): JwtManagerConfig =
    JwtManagerConfig.loadOrThrow(rootConfig.getConfig(location))

  def none() = JwtManagerConfig(JwtIssuerConfig.none(), JwtVerifierConfig.none())

  def loadOrThrow(location: String): JwtManagerConfig = ConfigFactory.load().getConfig(location).pipe(loadOrThrow)

  def loadOrThrow(config: Config): JwtManagerConfig =
    JwtManagerConfig(JwtIssuerConfig.loadOrThrow(config), JwtVerifierConfig.loadOrThrow(config))
}

package io.oath.jwt.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.chaining.scalaUtilChainingOps

final case class JwtManagerConfig(issuer: JwtIssuerConfig, verifier: JwtVerifierConfig)

object JwtManagerConfig {

  def loadOrThrow(location: String): JwtManagerConfig = ConfigFactory.load().getConfig(location).pipe(loadOrThrow)

  def loadOrThrow(config: Config): JwtManagerConfig =
    JwtManagerConfig(JwtIssuerConfig.loadOrThrow(config), JwtVerifierConfig.loadOrThrow(config))
}

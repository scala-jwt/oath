package io.oath.juror.config

import io.oath.jwt.config._

private[juror] object ConfigLoader {

  def loadOrThrowIssuer(configLocation: String): JwtIssuerConfig =
    JwtIssuerConfig.loadOrThrow(rootConfig.getConfig(configLocation))

  def loadOrThrowVerifier(configLocation: String): JwtVerifierConfig =
    JwtVerifierConfig.loadOrThrow(rootConfig.getConfig(configLocation))

  def loadOrThrowManager(configLocation: String): JwtManagerConfig =
    JwtManagerConfig.loadOrThrow(rootConfig.getConfig(configLocation))
}

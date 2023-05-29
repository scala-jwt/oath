package io.oath

import io.oath.config.ConfigLoader
import io.oath.jwt.{JwtVerifier => JVerifier}

import scala.util.chaining.scalaUtilChainingOps

final class OathVerifier[A] private (mapping: Map[A, JVerifier]) {

  def as[S <: A](tokenType: S): JwtVerifier[S] = mapping(tokenType)
}

object OathVerifier {

  def none[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): OathVerifier[A] =
    tokenEnumEntry.values
      .map(_ -> new JVerifier(ConfigLoader.noneVerifier()))
      .toMap
      .pipe(mapping => new OathVerifier(mapping))

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): OathVerifier[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.loadOrThrowVerifier(configLocation))
      .mapValues(config => new JVerifier(config))
      .toMap
      .pipe(mapping => new OathVerifier(mapping))

}

package io.oath

import io.oath.config.ConfigLoader
import io.oath.jwt.{JwtManager => JManager}

import scala.util.chaining.scalaUtilChainingOps

final class OathManager[A] private (mapping: Map[A, JManager]) {

  def as[S <: A](tokenType: S): JwtManager[S] = mapping(tokenType)
}

object OathManager {

  def none[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): OathManager[A] =
    tokenEnumEntry.values
      .map(_ -> new JManager(ConfigLoader.noneManager()))
      .toMap
      .pipe(mapping => new OathManager(mapping))

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): OathManager[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.loadOrThrowManager(configLocation))
      .mapValues(config => new JManager(config))
      .toMap
      .pipe(mapping => new OathManager(mapping))

}

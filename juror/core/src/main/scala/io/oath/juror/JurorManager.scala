package io.oath.juror

import io.oath.juror.config.ConfigLoader
import io.oath.jwt.{JwtManager => JManager}

import scala.util.chaining.scalaUtilChainingOps

final class JurorManager[A] private (mapping: Map[A, JManager]) {

  def as[S <: A](tokenType: S): JwtManager[S] = mapping(tokenType)
}

object JurorManager {

  def none[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorManager[A] =
    tokenEnumEntry.values
      .map(_ -> new JManager(ConfigLoader.noneManager()))
      .toMap
      .pipe(mapping => new JurorManager(mapping))

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorManager[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.loadOrThrowManager(configLocation))
      .mapValues(config => new JManager(config))
      .toMap
      .pipe(mapping => new JurorManager(mapping))

}

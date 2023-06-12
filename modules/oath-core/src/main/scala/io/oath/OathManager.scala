package io.oath

import io.oath.config.ConfigLoader
import io.oath.jwt.{JwtManager => JManager}

import scala.util.chaining.scalaUtilChainingOps

final class OathManager[A] private (mapping: Map[A, JManager]) {

  def as[S <: A](tokenType: S): JwtManager[S] = mapping(tokenType)
}

object OathManager {

  def none[A <: OathEnumEntry](tokenEnum: OathEnum[A]): OathManager[A] =
    tokenEnum.tokenValues
      .map(_ -> new JManager(ConfigLoader.noneManager()))
      .toMap
      .pipe(mapping => new OathManager(mapping))

  def createOrFail[A <: OathEnumEntry](tokenEnum: OathEnum[A]): OathManager[A] =
    tokenEnum.tokenValues.map { tokenType =>
      tokenType -> ConfigLoader.loadOrThrowManager(tokenType.configName).pipe(new JManager(_))
    }.toMap
      .pipe(mapping => new OathManager(mapping))
}

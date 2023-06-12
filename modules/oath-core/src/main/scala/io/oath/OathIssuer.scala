package io.oath

import io.oath.config.ConfigLoader
import io.oath.jwt.{JwtIssuer => JIssuer}

import scala.util.chaining.scalaUtilChainingOps

final class OathIssuer[A] private (mapping: Map[A, JIssuer]) {

  def as[S <: A](tokenType: S): JwtIssuer[S] = mapping(tokenType)
}

object OathIssuer {

  def none[A <: OathEnumEntry](tokenEnum: OathEnum[A]): OathIssuer[A] =
    tokenEnum.tokenValues
      .map(_ -> new JIssuer(ConfigLoader.noneIssuer()))
      .toMap
      .pipe(mapping => new OathIssuer(mapping))

  def createOrFail[A <: OathEnumEntry](tokenEnum: OathEnum[A]): OathIssuer[A] =
    tokenEnum.tokenValues.map { tokenType =>
      tokenType -> ConfigLoader.loadOrThrowIssuer(tokenType.configName).pipe(new JIssuer(_))
    }.toMap
      .pipe(mapping => new OathIssuer(mapping))
}

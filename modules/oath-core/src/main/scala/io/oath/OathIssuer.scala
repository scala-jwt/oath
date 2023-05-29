package io.oath

import io.oath.config.ConfigLoader
import io.oath.jwt.{JwtIssuer => JIssuer}

import scala.util.chaining.scalaUtilChainingOps

final class OathIssuer[A] private (mapping: Map[A, JIssuer]) {

  def as[S <: A](tokenType: S): JwtIssuer[S] = mapping(tokenType)
}

object OathIssuer {

  def none[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): OathIssuer[A] =
    tokenEnumEntry.values
      .map(_ -> new JIssuer(ConfigLoader.noneIssuer()))
      .toMap
      .pipe(mapping => new OathIssuer(mapping))

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): OathIssuer[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.loadOrThrowIssuer(configLocation))
      .mapValues(config => new JIssuer(config))
      .toMap
      .pipe(mapping => new OathIssuer(mapping))

}

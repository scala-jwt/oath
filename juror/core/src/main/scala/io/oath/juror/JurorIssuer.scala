package io.oath.juror

import io.oath.juror.config.ConfigLoader
import io.oath.jwt.{JwtIssuer => JIssuer}

import scala.util.chaining.scalaUtilChainingOps

final class JurorIssuer[A] private (mapping: Map[A, JIssuer]) {

  def as[S <: A](tokenType: S): JwtIssuer[S] = mapping(tokenType)
}

object JurorIssuer {

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorIssuer[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.loadOrThrowIssuer(configLocation))
      .mapValues(config => new JIssuer(config))
      .toMap
      .pipe(mapping => new JurorIssuer(mapping))

}

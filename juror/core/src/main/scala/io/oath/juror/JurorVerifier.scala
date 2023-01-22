package io.oath.juror

import io.oath.juror.config.ConfigLoader
import io.oath.jwt.{JwtVerifier => JVerifier}

import scala.util.chaining.scalaUtilChainingOps

final class JurorVerifier[A] private (mapping: Map[A, JVerifier]) {

  def as[S <: A](tokenType: S): JwtVerifier[S] = mapping(tokenType)
}

object JurorVerifier {

  def createOrFail[A <: TokenEnumEntry](tokenEnumEntry: TokenEnum[A]): JurorVerifier[A] =
    tokenEnumEntry.mapping.view
      .mapValues(configLocation => ConfigLoader.loadOrThrowVerifier(configLocation))
      .mapValues(config => new JVerifier(config))
      .toMap
      .pipe(mapping => new JurorVerifier(mapping))

}

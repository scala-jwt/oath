package io.oath

import io.oath.config.ConfigLoader
import io.oath.jwt.{JwtVerifier => JVerifier}

import scala.util.chaining.scalaUtilChainingOps

final class OathVerifier[A] private (mapping: Map[A, JVerifier]) {

  def as[S <: A](tokenType: S): JwtVerifier[S] = mapping(tokenType)
}

object OathVerifier {

  def none[A <: OathEnumEntry](tokenEnum: OathEnum[A]): OathVerifier[A] =
    tokenEnum.tokenValues
      .map(_ -> new JVerifier(ConfigLoader.noneVerifier()))
      .toMap
      .pipe(mapping => new OathVerifier(mapping))

  def createOrFail[A <: OathEnumEntry](tokenEnum: OathEnum[A]): OathVerifier[A] =
    tokenEnum.tokenValues.map { tokenType =>
      tokenType -> ConfigLoader.loadOrThrowVerifier(tokenType.configName).pipe(new JVerifier(_))
    }.toMap
      .pipe(mapping => new OathVerifier(mapping))

}

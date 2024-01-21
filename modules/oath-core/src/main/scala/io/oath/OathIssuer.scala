package io.oath

import io.oath.config.JwtIssuerConfig

import scala.util.chaining.scalaUtilChainingOps

final class OathIssuer[A](mapping: Map[A, JwtIssuer]):
  def as[S <: A](tokenType: S): JIssuer[S] = mapping(tokenType)

object OathIssuer:
  inline def none[A]: OathIssuer[A] =
    getEnumValues[A].map { case (tokenType, _) =>
      tokenType -> JwtIssuer(JwtIssuerConfig.none())
    }.toMap
      .pipe(mapping => OathIssuer(mapping))

  inline def createOrFail[A]: OathIssuer[A] =
    getEnumValues[A].map { case (tokenType, tokenConfig) =>
      tokenType -> JwtIssuerConfig.loadOrThrowOath(tokenConfig).pipe(JwtIssuer(_))
    }.toMap
      .pipe(mapping => OathIssuer(mapping))

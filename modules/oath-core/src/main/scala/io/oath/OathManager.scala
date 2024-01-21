package io.oath

import io.oath.config.*

import scala.util.chaining.scalaUtilChainingOps

final class OathManager[A](mapping: Map[A, JwtManager]):
  def as[S <: A](tokenType: S): JManager[S] = mapping(tokenType)

object OathManager:
  inline def none[A]: OathManager[A] =
    getEnumValues[A].map { case (tokenType, _) =>
      tokenType -> JwtManager(JwtManagerConfig.none())
    }.toMap
      .pipe(mapping => OathManager(mapping))

  inline def createOrFail[A]: OathManager[A] =
    getEnumValues[A].map { case (tokenType, tokenConfig) =>
      tokenType -> JwtManagerConfig.loadOrThrowOath(tokenConfig).pipe(JwtManager(_))
    }.toMap
      .pipe(mapping => OathManager(mapping))

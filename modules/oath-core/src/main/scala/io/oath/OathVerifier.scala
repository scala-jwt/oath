package io.oath

import io.oath.config.*

import scala.util.chaining.scalaUtilChainingOps

final class OathVerifier[A](mapping: Map[A, JwtVerifier]):
  def as[S <: A](tokenType: S): JVerifier[S] = mapping(tokenType)

object OathVerifier:
  inline def none[A]: OathVerifier[A] =
    getEnumValues[A].map { case (tokenType, _) =>
      tokenType -> JwtVerifier(JwtVerifierConfig.none())
    }.toMap
      .pipe(mapping => OathVerifier(mapping))

  inline def createOrFail[A]: OathVerifier[A] =
    getEnumValues[A].map { case (tokenType, tokenConfig) =>
      tokenType -> JwtVerifierConfig.loadOrThrowOath(tokenConfig).pipe(JwtVerifier(_))
    }.toMap
      .pipe(mapping => OathVerifier(mapping))

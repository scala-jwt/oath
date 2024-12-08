package io.oath

import io.oath.OathIssuer.JIssuer
import io.oath.config.JwtIssuerConfig
import io.oath.macros.OathEnum

import scala.util.chaining.scalaUtilChainingOps

trait OathIssuer[A] {
  def as[S <: A](tokenType: S): JIssuer[S]
}

object OathIssuer {
  // Type aliases with extra information, useful to determine the token type.
  type JIssuer[_] = JwtIssuer

  private final class JavaJwtOathIssuer[A](mapping: Map[A, JwtIssuer]) extends OathIssuer[A] {
    def as[S <: A](tokenType: S): JIssuer[S] = mapping(tokenType)
  }

  def none[E: OathEnum]: OathIssuer[E] =
    summon[OathEnum[E]].values.map { case (tokenType, _) =>
      tokenType -> JwtIssuer(JwtIssuerConfig.none())
    }.pipe(mapping => new JavaJwtOathIssuer(mapping))

  def createOrFail[E: OathEnum]: OathIssuer[E] =
    summon[OathEnum[E]].values.map { case (tokenType, tokenConfig) =>
      tokenType -> JwtIssuerConfig.loadOrThrowOath(tokenConfig).pipe(JwtIssuer(_))
    }.pipe(mapping => new JavaJwtOathIssuer(mapping))
}

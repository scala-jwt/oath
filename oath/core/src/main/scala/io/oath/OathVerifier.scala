package io.oath

import io.oath.OathVerifier.JVerifier
import io.oath.config.*
import io.oath.macros.OathEnum

import scala.util.chaining.scalaUtilChainingOps

trait OathVerifier[A] {
  def as[S <: A](tokenType: S): JVerifier[S]

}

object OathVerifier {
  // Type aliases with extra information, useful to determine the token type.
  type JVerifier[_] = JwtVerifier

  private final class JavaJwtOathVerifier[A](mapping: Map[A, JwtVerifier]) extends OathVerifier[A] {
    def as[S <: A](tokenType: S): JVerifier[S] = mapping(tokenType)
  }

  def none[E: OathEnum]: OathVerifier[E] =
    summon[OathEnum[E]].values.map { case (tokenType, _) =>
      tokenType -> JwtVerifier(JwtVerifierConfig.none())
    }.pipe(mapping => new JavaJwtOathVerifier(mapping))

  def createOrFail[E: OathEnum]: OathVerifier[E] =
    summon[OathEnum[E]].values.map { case (tokenType, tokenConfig) =>
      tokenType -> JwtVerifierConfig
        .loadOrThrowOath(tokenConfig)
        .pipe(JwtVerifier(_))
    }.pipe(mapping => new JavaJwtOathVerifier(mapping))
}

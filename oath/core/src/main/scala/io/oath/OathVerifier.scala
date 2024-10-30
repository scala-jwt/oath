package io.oath

import io.oath.OathVerifier.JVerifier
import io.oath.config.*

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

  def none[A](using
      m: scala.deriving.Mirror.SumOf[A]
  ): OathVerifier[A] =
    getEnumValues[A].map { case (tokenType, _) =>
      tokenType -> JwtVerifier(JwtVerifierConfig.none())
    }.toMap
      .pipe(mapping => new JavaJwtOathVerifier(mapping))

  def createOrFail[A](using
      m: scala.deriving.Mirror.SumOf[A]
  ): OathVerifier[A] =
    getEnumValues[A].map { case (tokenType, tokenConfig) =>
      tokenType -> JwtVerifierConfig.loadOrThrowOath(tokenConfig).pipe(JwtVerifier(_))
    }.toMap
      .pipe(mapping => new JavaJwtOathVerifier(mapping))
}

package io.oath

import io.oath.OathManager.JManager
import io.oath.config.*

import scala.util.chaining.scalaUtilChainingOps

trait OathManager[A] {
  def as[S <: A](tokenType: S): JManager[S]
}

object OathManager {
  // Type aliases with extra information, useful to determine the token type.
  type JManager[_] = JwtManager

  private final class JavaJwtOathManager[A](mapping: Map[A, JwtManager]) extends OathManager[A] {
    def as[S <: A](tokenType: S): JManager[S] = mapping(tokenType)
  }

  def none[A](using
      m: scala.deriving.Mirror.SumOf[A]
  ): OathManager[A] =
    getEnumValues[A].map { case (tokenType, _) =>
      tokenType -> JwtManager(JwtManagerConfig.none())
    }.toMap
      .pipe(mapping => new JavaJwtOathManager(mapping))

  def createOrFail[A](using
      m: scala.deriving.Mirror.SumOf[A]
  ): OathManager[A] =
    getEnumValues[A].map { case (tokenType, tokenConfig) =>
      tokenType -> JwtManagerConfig.loadOrThrowOath(tokenConfig).pipe(JwtManager(_))
    }.toMap
      .pipe(mapping => new JavaJwtOathManager(mapping))
}

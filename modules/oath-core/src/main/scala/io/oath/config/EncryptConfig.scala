package io.oath.config

import com.typesafe.config.Config

import scala.util.chaining.*

private[oath] final case class EncryptConfig(secret: String)

object EncryptConfig:
  private def loadOrThrowEncryptConfig(encryptScoped: Config): EncryptConfig =
    encryptScoped
      .getMaybeNonEmptyString("secret")
      .getOrElse(throw new IllegalArgumentException("Empty string for secret is not allowed for encryption!"))
      .pipe(EncryptConfig.apply)

  private[config] def loadOrThrow(encryptScoped: Config): EncryptConfig = loadOrThrowEncryptConfig(encryptScoped)

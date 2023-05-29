package io.oath.config

import com.typesafe.config.Config

import scala.util.chaining.scalaUtilChainingOps

object EncryptionLoader {

  final case class EncryptConfig(secret: String)

  private def loadOrThrowEncryptConfig(encryptScoped: Config): EncryptConfig =
    encryptScoped
      .getMaybeNonEmptyString("secret")
      .getOrElse(throw new IllegalArgumentException("Empty string for secret is not allowed for encryption!"))
      .pipe(EncryptConfig)

  private[config] def loadOrThrow(encryptScoped: Config): EncryptConfig = loadOrThrowEncryptConfig(encryptScoped)
}

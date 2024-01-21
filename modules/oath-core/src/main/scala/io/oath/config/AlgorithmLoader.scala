package io.oath.config

import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.Config
import org.bouncycastle.util.io.pem.PemReader

import java.io.{File, FileReader}
import java.security.interfaces.*
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyFactory, PrivateKey, PublicKey}
import scala.util.Using
import scala.util.chaining.*

object AlgorithmLoader:
  private val SecretKeyConfigValue         = "secret-key"
  private val PrivateKeyPemPathConfigValue = "private-key-pem-path"
  private val PublicKeyPemPathConfigValue  = "public-key-pem-path"
  private val RSAKeyFactoryInstance        = "RSA"
  private val ECKeyFactoryInstance         = "EC"

  private val RSAKeyFactory = KeyFactory.getInstance(RSAKeyFactoryInstance)
  private val ECKeyFactory  = KeyFactory.getInstance(ECKeyFactoryInstance)

  private def loadSecretKeyOrThrow(algorithmScoped: Config): String =
    algorithmScoped.getString(SecretKeyConfigValue)

  private def loadRSAKeyOrThrow(
      algorithmScoped: Config,
      forIssuing: Boolean,
  ): (Option[RSAPrivateKey], Option[RSAPublicKey]) =
    if forIssuing then
      val privateKey: RSAPrivateKey = loadPrivateKey(algorithmScoped, RSAKeyFactory)
        .map(_.asInstanceOf[RSAPrivateKey])
        .fold(error => throw new IllegalArgumentException(s"Fail to load RSA Private key pem file: $error"), identity)
      (Some(privateKey), None)
    else
      val publicKey: RSAPublicKey = loadPublicKey(algorithmScoped, RSAKeyFactory)
        .map(_.asInstanceOf[RSAPublicKey])
        .fold(error => throw new IllegalArgumentException(s"Fail to load RSA Public key pem file: $error"), identity)
      (None, Some(publicKey))

  private def loadECKeyOrThrow(
      algorithmScoped: Config,
      forIssuing: Boolean,
  ): (Option[ECPrivateKey], Option[ECPublicKey]) =
    if forIssuing then
      val privateKey: ECPrivateKey = loadPrivateKey(algorithmScoped, ECKeyFactory)
        .map(_.asInstanceOf[ECPrivateKey])
        .fold(error => throw new IllegalArgumentException(s"Failed to load EC Private key pem file: $error"), identity)
      (Some(privateKey), None)
    else
      val publicKey: ECPublicKey = loadPublicKey(algorithmScoped, ECKeyFactory)
        .map(_.asInstanceOf[ECPublicKey])
        .fold(error => throw new IllegalArgumentException(s"Failed to load EC Public key pem file: $error"), identity)
      (None, Some(publicKey))

  private def loadPublicKey(algorithmScoped: Config, keyFactory: KeyFactory): Either[String, PublicKey] =
    algorithmScoped
      .getString(PublicKeyPemPathConfigValue)
      .pipe(privateKeyPemPath =>
        Using(new FileReader(new File(privateKeyPemPath))) { reader =>
          new PemReader(reader)
            .pipe(_.readPemObject().getContent)
            .pipe(new X509EncodedKeySpec(_))
            .pipe(keyFactory.generatePublic)
        }.toEither.left
          .map(error => s"public key pem file error [${error.getMessage}]")
      )

  private def loadPrivateKey(signatureScoped: Config, keyFactory: KeyFactory): Either[String, PrivateKey] =
    signatureScoped
      .getString(PrivateKeyPemPathConfigValue)
      .pipe(privateKeyPemPath =>
        Using(new FileReader(new File(privateKeyPemPath))) { reader =>
          new PemReader(reader)
            .pipe(_.readPemObject().getContent)
            .pipe(new PKCS8EncodedKeySpec(_))
            .pipe(keyFactory.generatePrivate)
        }.toEither.left
          .map(error => s"private key pem file: ${error.getMessage}")
      )

  private[oath] def loadOrThrow(algorithmScoped: Config, isIssuer: Boolean): Algorithm =
    val algorithm = algorithmScoped.getString("name")
    algorithm.trim.toUpperCase match
      case "HS256" =>
        loadSecretKeyOrThrow(algorithmScoped).pipe(Algorithm.HMAC256)
      case "HS384" =>
        loadSecretKeyOrThrow(algorithmScoped).pipe(Algorithm.HMAC384)
      case "HS512" =>
        loadSecretKeyOrThrow(algorithmScoped).pipe(Algorithm.HMAC512)
      case "RS256" =>
        val (maybePrivateKey, maybePublicKey) = loadRSAKeyOrThrow(algorithmScoped, isIssuer)
        Algorithm.RSA256(maybePublicKey.orNull, maybePrivateKey.orNull)
      case "RS384" =>
        val (maybePrivateKey, maybePublicKey) = loadRSAKeyOrThrow(algorithmScoped, isIssuer)
        Algorithm.RSA384(maybePublicKey.orNull, maybePrivateKey.orNull)
      case "RS512" =>
        val (maybePrivateKey, maybePublicKey) = loadRSAKeyOrThrow(algorithmScoped, isIssuer)
        Algorithm.RSA512(maybePublicKey.orNull, maybePrivateKey.orNull)
      case "ES256" =>
        val (maybePrivateKey, maybePublicKey) = loadECKeyOrThrow(algorithmScoped, isIssuer)
        Algorithm.ECDSA256(maybePublicKey.orNull, maybePrivateKey.orNull)
      case "ES384" =>
        val (maybePrivateKey, maybePublicKey) = loadECKeyOrThrow(algorithmScoped, isIssuer)
        Algorithm.ECDSA384(maybePublicKey.orNull, maybePrivateKey.orNull)
      case "ES512" =>
        val (maybePrivateKey, maybePublicKey) = loadECKeyOrThrow(algorithmScoped, isIssuer)
        Algorithm.ECDSA512(maybePublicKey.orNull, maybePrivateKey.orNull)
      case "NONE" => Algorithm.none()
      case _ =>
        throw new IllegalArgumentException(s"Unsupported signature algorithm: $algorithm")

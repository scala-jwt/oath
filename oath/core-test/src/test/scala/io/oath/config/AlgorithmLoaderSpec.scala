package io.oath.config

import com.auth0.jwt.JWT
import com.typesafe.config.ConfigFactory
import io.oath.test.*

class AlgorithmLoaderSpec extends AnyWordSpecBase, PropertyBasedTesting {
  inline val AlgorithmConfigLocation = "algorithm"

  "AlgorithmLoader" should {
    "load none encryption algorithm config" in forAll { (issuer: String) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-none").getConfig(AlgorithmConfigLocation)
      val issuingAlgorithm      = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = true)
      val verifyingAlgorithm    = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = false)

      val token: String  = JWT.create().withIssuer(issuer).sign(issuingAlgorithm)
      val verifiedIssuer = JWT.require(verifyingAlgorithm).build().verify(token).getIssuer

      issuingAlgorithm.getName shouldBe "none"
      verifyingAlgorithm.getName shouldBe "none"
      verifiedIssuer shouldBe issuer
      token should not be empty
    }

    "load RSXXX encryption algorithm with secret key" in forAll { (issuer: String) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-rsxxx").getConfig(AlgorithmConfigLocation)
      val issuingAlgorithm      = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = true)
      val verifyingAlgorithm    = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = false)

      val token: String  = JWT.create().withIssuer(issuer).sign(issuingAlgorithm)
      val verifiedIssuer = JWT.require(verifyingAlgorithm).build().verify(token).getIssuer

      issuingAlgorithm.getName shouldBe "RS256"
      verifyingAlgorithm.getName shouldBe "RS256"
      verifiedIssuer shouldBe issuer
      token should not be empty
    }

    "load HSXXX encryption algorithm with secret key" in forAll { (issuer: String) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-hsxxx").getConfig(AlgorithmConfigLocation)
      val issuingAlgorithm      = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = true)
      val verifyingAlgorithm    = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = false)

      val token: String  = JWT.create().withIssuer(issuer).sign(issuingAlgorithm)
      val verifiedIssuer = JWT.require(verifyingAlgorithm).build().verify(token).getIssuer

      issuingAlgorithm.getName shouldBe "HS256"
      verifyingAlgorithm.getName shouldBe "HS256"
      verifiedIssuer shouldBe issuer
      token should not be empty
    }

    "load ES256 encryption algorithm with secret key" in forAll { (issuer: String) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-es256").getConfig(AlgorithmConfigLocation)
      val issuingAlgorithm      = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = true)
      val verifyingAlgorithm    = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = false)

      val token: String  = JWT.create().withIssuer(issuer).sign(issuingAlgorithm)
      val verifiedIssuer = JWT.require(verifyingAlgorithm).build().verify(token).getIssuer

      issuingAlgorithm.getName shouldBe "ES256"
      verifyingAlgorithm.getName shouldBe "ES256"
      verifiedIssuer shouldBe issuer
      token should not be empty
    }

    "load ES384 encryption algorithm with secret key" in forAll { (issuer: String) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-es384").getConfig(AlgorithmConfigLocation)
      val issuingAlgorithm      = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = true)
      val verifyingAlgorithm    = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = false)

      val token: String  = JWT.create().withIssuer(issuer).sign(issuingAlgorithm)
      val verifiedIssuer = JWT.require(verifyingAlgorithm).build().verify(token).getIssuer

      issuingAlgorithm.getName shouldBe "ES384"
      verifyingAlgorithm.getName shouldBe "ES384"
      verifiedIssuer shouldBe issuer
      token should not be empty
    }

    "load ES512 encryption algorithm with secret key" in forAll { (issuer: String) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-es512").getConfig(AlgorithmConfigLocation)
      val issuingAlgorithm      = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = true)
      val verifyingAlgorithm    = AlgorithmLoader.loadOrThrow(algorithmScopedConfig, isIssuer = false)

      val token: String  = JWT.create().withIssuer(issuer).sign(issuingAlgorithm)
      val verifiedIssuer = JWT.require(verifyingAlgorithm).build().verify(token).getIssuer

      issuingAlgorithm.getName shouldBe "ES512"
      verifyingAlgorithm.getName shouldBe "ES512"
      verifiedIssuer shouldBe issuer
      token should not be empty
    }

    "fail to load unsupported algorithm type" in forAll { (bool: Boolean) =>
      val algorithmScopedConfig = ConfigFactory.load("algorithm-unsupported").getConfig(AlgorithmConfigLocation)
      the[IllegalArgumentException] thrownBy AlgorithmLoader
        .loadOrThrow(algorithmScopedConfig, bool) should have message "Unsupported signature algorithm: Boom"
    }
  }
}

package io.oath.test

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.{JWT, JWTCreator}
import io.oath.*
import io.oath.config.JwtVerifierConfig.*
import io.oath.config.{EncryptConfig, JwtVerifierConfig}
import io.oath.syntax.*
import io.oath.test.NestedHeader.{SimpleHeader, nestedHeaderEncoder}
import io.oath.test.NestedPayload.{SimplePayload, nestedPayloadEncoder}
import io.oath.testkit.{AnyWordSpecBase, PropertyBasedTesting}
import io.oath.utils.*

import scala.util.chaining.scalaUtilChainingOps

class JwtVerifierSpec extends AnyWordSpecBase, PropertyBasedTesting, ClockHelper, CodecUtils:

  val defaultConfig =
    JwtVerifierConfig(
      Algorithm.none(),
      None,
      ProvidedWithConfig(None, None, Nil),
      LeewayWindowConfig(None, None, None, None),
    )

  def setRegisteredClaims(builder: JWTCreator.Builder, config: JwtVerifierConfig): TestData =
    val now       = getInstantNowSeconds
    val leeway    = config.leewayWindow.leeway.map(leeway => now.plusSeconds(leeway.toSeconds - 1))
    val expiresAt = config.leewayWindow.expiresAt.map(expiresAt => now.plusSeconds(expiresAt.toSeconds - 1))
    val notBefore = config.leewayWindow.notBefore.map(notBefore => now.plusSeconds(notBefore.toSeconds - 1))
    val issueAt   = config.leewayWindow.issuedAt.map(issueAt => now.plusSeconds(issueAt.toSeconds - 1))

    val registeredClaims = RegisteredClaims(
      config.providedWith.issuerClaim,
      config.providedWith.subjectClaim,
      config.providedWith.audienceClaims,
      expiresAt orElse leeway,
      notBefore orElse leeway,
      issueAt orElse leeway,
      None,
    )

    val builderWithRegistered = builder
      .tap(builder => registeredClaims.iss.map(str => builder.withIssuer(str)))
      .tap(builder => registeredClaims.sub.map(str => builder.withSubject(str)))
      .tap(builder => builder.withAudience(registeredClaims.aud: _*))
      .tap(builder => registeredClaims.exp.map(builder.withExpiresAt))
      .tap(builder => registeredClaims.nbf.map(builder.withNotBefore))
      .tap(builder => registeredClaims.iat.map(builder.withIssuedAt))

    TestData(registeredClaims, builderWithRegistered)

  "JwtVerifier" should:
    "verify token with prerequisite configurations" in forAll: (config: JwtVerifierConfig) =>
      val jwtVerifier = new JwtVerifier(config.copy(encrypt = None))

      val testData = setRegisteredClaims(JWT.create(), config)

      val token = testData.builder.sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt(token.toToken).value

      verified.registered shouldBe testData.registeredClaims

    "verify a token with header" in forAll: (nestedHeader: NestedHeader, config: JwtVerifierConfig) =>
      val jwtVerifier = new JwtVerifier(config.copy(encrypt = None))

      val testData = setRegisteredClaims(JWT.create(), config)

      val token = testData.builder
        .withHeader(unsafeParseJsonToJavaMap(nestedHeaderEncoder.encode(nestedHeader)))
        .sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)

      verified.value shouldBe nestedHeader.toClaimsH.copy(registered = testData.registeredClaims)

    "verify a token with header that is encrypted" in forAll:
      (nestedHeader: NestedHeader, config: JwtVerifierConfig, encryptConfig: EncryptConfig) =>
        val jwtVerifier = new JwtVerifier(config.copy(encrypt = Some(encryptConfig)))

        val testData = setRegisteredClaims(JWT.create(), config)

        val token = testData.builder
          .withHeader(unsafeParseJsonToJavaMap(nestedHeaderEncoder.encode(nestedHeader)))
          .sign(config.algorithm)
          .pipe(token => EncryptionUtils.encryptAES(token, encryptConfig.secret))
          .value

        val verified = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)

        verified.value shouldBe nestedHeader.toClaimsH.copy(registered = testData.registeredClaims)

    "verify a token with payload" in forAll: (nestedPayload: NestedPayload, config: JwtVerifierConfig) =>
      val jwtVerifier = new JwtVerifier(config.copy(encrypt = None))

      val testData = setRegisteredClaims(JWT.create(), config)

      val token = testData.builder
        .withPayload(unsafeParseJsonToJavaMap(nestedPayloadEncoder.encode(nestedPayload)))
        .sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)

      verified.value shouldBe nestedPayload.toClaimsP.copy(registered = testData.registeredClaims)

    "verify a token with payload that is encrypted" in forAll:
      (nestedPayload: NestedPayload, config: JwtVerifierConfig, encryptConfig: EncryptConfig) =>
        val jwtVerifier = new JwtVerifier(config.copy(encrypt = Some(encryptConfig)))

        val testData = setRegisteredClaims(JWT.create(), config)

        val token = testData.builder
          .withPayload(unsafeParseJsonToJavaMap(nestedPayloadEncoder.encode(nestedPayload)))
          .sign(config.algorithm)
          .pipe(token => EncryptionUtils.encryptAES(token, encryptConfig.secret))
          .value

        val verified = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)

        verified.value shouldBe nestedPayload.toClaimsP.copy(registered = testData.registeredClaims)

    "verify a token with header & payload" in forAll:
      (nestedHeader: NestedHeader, nestedPayload: NestedPayload, config: JwtVerifierConfig) =>
        val jwtVerifier = new JwtVerifier(config.copy(encrypt = None))

        val testData = setRegisteredClaims(JWT.create(), config)

        val token = testData.builder
          .withPayload(unsafeParseJsonToJavaMap(nestedPayloadEncoder.encode(nestedPayload)))
          .withHeader(unsafeParseJsonToJavaMap(nestedHeaderEncoder.encode(nestedHeader)))
          .sign(config.algorithm)

        val verified =
          jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)

        verified.value shouldBe (nestedHeader, nestedPayload).toClaimsHP.copy(registered = testData.registeredClaims)

    "verify a token with header & payload that is encrypted" in forAll:
      (
          nestedHeader: NestedHeader,
          nestedPayload: NestedPayload,
          config: JwtVerifierConfig,
          encryptConfig: EncryptConfig,
      ) =>
        val jwtVerifier = new JwtVerifier(config.copy(encrypt = Some(encryptConfig)))

        val testData = setRegisteredClaims(JWT.create(), config)

        val token = testData.builder
          .withPayload(unsafeParseJsonToJavaMap(nestedPayloadEncoder.encode(nestedPayload)))
          .withHeader(unsafeParseJsonToJavaMap(nestedHeaderEncoder.encode(nestedHeader)))
          .sign(config.algorithm)
          .pipe(token => EncryptionUtils.encryptAES(token, encryptConfig.secret))
          .value

        val verified =
          jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)

        verified.value shouldBe (nestedHeader, nestedPayload).toClaimsHP.copy(registered = testData.registeredClaims)

    "fail to verify a token that is encrypted" in:
      val encryptConfig = defaultConfig.copy(encrypt = Some(EncryptConfig("secret")))
      val jwtVerifier   = new JwtVerifier(encryptConfig)

      val token = JWT
        .create()
        .sign(encryptConfig.algorithm)
        .pipe(token => EncryptionUtils.encryptAES(token, encryptConfig.encrypt.value.secret))
        .value

      val outOfRangeLongerToken  = token + "H"
      val outOfRangeShorterToken = token.take(token.length - 1)
      val notValid               = outOfRangeShorterToken + "."

      val failedOutOfRangeLonger  = jwtVerifier.verifyJwt(outOfRangeLongerToken.toToken)
      val failedOutOfRangeShorter = jwtVerifier.verifyJwt(outOfRangeShorterToken.toToken)
      val failedNotValid          = jwtVerifier.verifyJwt(notValid.toToken)

      failedOutOfRangeLonger.left.value shouldBe a[JwtVerifyError.DecryptionError]
      failedOutOfRangeShorter.left.value shouldBe a[JwtVerifyError.DecryptionError]
      failedNotValid.left.value shouldBe a[JwtVerifyError.DecryptionError]

    "fail to decode a token with header" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val header = """{"name": "name"}"""
      val token = JWT
        .create()
        .withHeader(unsafeParseJsonToJavaMap(header))
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)

      verified shouldBe Left(JwtVerifyError.DecodingError("DecodingFailure at .mapping: Missing required field", null))

    "fail to decode a token with payload" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val payload = """{"name": "name"}"""
      val token = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(payload))
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)

      verified shouldBe Left(JwtVerifyError.DecodingError("DecodingFailure at .mapping: Missing required field", null))

    "fail to decode a token with header & payload" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val header = """{"name": "name"}"""
      val token = JWT
        .create()
        .withHeader(unsafeParseJsonToJavaMap(header))
        .sign(defaultConfig.algorithm)

      val verified =
        jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)

      verified shouldBe Left(JwtVerifyError.DecodingError("DecodingFailure at .mapping: Missing required field", null))

    "fail to decode a token with header if exception raised in decoder" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val token = JWT
        .create()
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt[SimpleHeader](token.toTokenH)

      verified.left.value.error shouldBe "Boom"

    "fail to decode a token with payload if exception raised in decoder" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val token = JWT
        .create()
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt[SimplePayload](token.toTokenP)

      verified.left.value.error shouldBe "Boom"

    "fail to decode a token with header & payload if exception raised in decoder" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val token = JWT
        .create()
        .sign(defaultConfig.algorithm)

      val verified =
        jwtVerifier.verifyJwt[SimpleHeader, SimplePayload](token.toTokenHP)

      verified.left.value.error shouldBe "Boom"

    "fail to verify token with VerificationError when provided with claims are not meet criteria" in:
      val config      = defaultConfig.copy(providedWith = defaultConfig.providedWith.copy(issuerClaim = Some("issuer")))
      val jwtVerifier = new JwtVerifier(config)

      val token = JWT
        .create()
        .sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt(token.toToken)

      verified.left.value shouldBe JwtVerifyError.VerificationError("The Claim 'iss' is not present in the JWT.")

    "fail to verify token with IllegalArgument when null algorithm is provided" in forAll:
      (config: JwtVerifierConfig) =>
        val jwtVerifier = new JwtVerifier(config.copy(algorithm = null, encrypt = None))

        val token = JWT
          .create()
          .sign(config.algorithm)

        val verified = jwtVerifier.verifyJwt(token.toToken)

        verified.left.value shouldBe JwtVerifyError.IllegalArgument("The Algorithm cannot be null.")

    "fail to verify token with AlgorithmMismatch when jwt header algorithm doesn't match with verify" in forAll:
      (config: JwtVerifierConfig) =>
        val jwtVerifier = new JwtVerifier(config.copy(algorithm = Algorithm.HMAC256("secret"), encrypt = None))

        val token = JWT
          .create()
          .sign(config.algorithm)

        val verified = jwtVerifier.verifyJwt(token.toToken)

        verified.left.value shouldBe
          JwtVerifyError
            .AlgorithmMismatch("The provided Algorithm doesn't match the one defined in the JWT's Header.")

    "fail to verify token with SignatureVerificationError when secrets provided are wrong" in forAll:
      (config: JwtVerifierConfig) =>
        val jwtVerifier = new JwtVerifier(config.copy(algorithm = Algorithm.HMAC256("secret2"), encrypt = None))

        val token = JWT
          .create()
          .sign(Algorithm.HMAC256("secret1"))

        val verified = jwtVerifier.verifyJwt(token.toToken)

        verified.left.value shouldBe
          JwtVerifyError
            .SignatureVerificationError(
              "The Token's Signature resulted invalid when verified using the Algorithm: HmacSHA256"
            )

    "fail to verify token with TokenExpired when JWT expires" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)

      val expiresAt = getInstantNowSeconds.minusSeconds(1)
      val token = JWT
        .create()
        .withExpiresAt(expiresAt)
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt(token.toToken)

      verified.left.value shouldBe
        JwtVerifyError
          .TokenExpired(s"The Token has expired on $expiresAt.")

    "fail to verify an empty string token" in:
      val jwtVerifier = new JwtVerifier(defaultConfig)
      val token       = ""
      val verified    = jwtVerifier.verifyJwt(token.toToken)
      val verifiedH   = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)
      val verifiedP   = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)
      val verifiedHP  = jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)

      verified.left.value shouldBe
        JwtVerifyError
          .VerificationError("JWT Token is empty.")

      verifiedH.left.value shouldBe
        JwtVerifyError
          .VerificationError("JWT Token is empty.")

      verifiedP.left.value shouldBe
        JwtVerifyError
          .VerificationError("JWT Token is empty.")

      verifiedHP.left.value shouldBe
        JwtVerifyError
          .VerificationError("JWT Token is empty.")

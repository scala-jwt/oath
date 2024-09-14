package io.oath

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.*
import com.auth0.jwt.{JWT, JWTCreator}
import io.oath.NestedHeader.SimpleHeader
import io.oath.NestedPayload.SimplePayload
import io.oath.config.JwtVerifierConfig
import io.oath.config.JwtVerifierConfig.*
import io.oath.syntax.all.*
import io.oath.testkit.*

import scala.util.chaining.scalaUtilChainingOps

class JwtVerifierSpec extends AnyWordSpecBase, PropertyBasedTesting, ClockHelper {

  val defaultConfig =
    JwtVerifierConfig(
      Algorithm.none(),
      ProvidedWithConfig(None, None, Nil),
      LeewayWindowConfig(None, None, None, None),
    )

  def setRegisteredClaims(builder: JWTCreator.Builder, config: JwtVerifierConfig): TestData = {
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
      .tap(builder => builder.withAudience(registeredClaims.aud*))
      .tap(builder => registeredClaims.exp.map(builder.withExpiresAt))
      .tap(builder => registeredClaims.nbf.map(builder.withNotBefore))
      .tap(builder => registeredClaims.iat.map(builder.withIssuedAt))

    TestData(registeredClaims, builderWithRegistered)
  }

  "JwtVerifier" should {
    "verify token with prerequisite configurations" in forAll { (config: JwtVerifierConfig) =>
      val jwtVerifier = JwtVerifier(config)
      val testData    = setRegisteredClaims(JWT.create(), config)
      val token       = testData.builder.sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt(token.toToken).value

      verified.registered shouldBe testData.registeredClaims
    }

    "verify a token with header" in forAll { (nestedHeader: NestedHeader, config: JwtVerifierConfig) =>
      val jwtVerifier = JwtVerifier(config)
      val testData    = setRegisteredClaims(JWT.create(), config)
      val token = testData.builder
        .withHeader(CodecHelper.unsafeParseJsonToJavaMap(NestedHeader.claimsEncoder.encode(nestedHeader)))
        .sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)

      verified.value shouldBe nestedHeader.toClaimsH.copy(registered = testData.registeredClaims)
    }

    "verify a token with payload" in forAll { (nestedPayload: NestedPayload, config: JwtVerifierConfig) =>
      val jwtVerifier = JwtVerifier(config)
      val testData    = setRegisteredClaims(JWT.create(), config)
      val token = testData.builder
        .withPayload(CodecHelper.unsafeParseJsonToJavaMap(NestedPayload.claimsEncoder.encode(nestedPayload)))
        .sign(config.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)

      verified.value shouldBe nestedPayload.toClaimsP.copy(registered = testData.registeredClaims)
    }

    "verify a token with header & payload" in forAll {
      (nestedHeader: NestedHeader, nestedPayload: NestedPayload, config: JwtVerifierConfig) =>
        val jwtVerifier = JwtVerifier(config)
        val testData    = setRegisteredClaims(JWT.create(), config)
        val token = testData.builder
          .withPayload(CodecHelper.unsafeParseJsonToJavaMap(NestedPayload.claimsEncoder.encode(nestedPayload)))
          .withHeader(CodecHelper.unsafeParseJsonToJavaMap(NestedHeader.claimsEncoder.encode(nestedHeader)))
          .sign(config.algorithm)

        val verified =
          jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)

        verified.value shouldBe (nestedHeader, nestedPayload).toClaimsHP.copy(registered = testData.registeredClaims)
    }

    "fail to decode a token with header" in {
      val jwtVerifier = JwtVerifier(defaultConfig)
      val header      = """{"name": "name"}"""
      val token = JWT
        .create()
        .withHeader(CodecHelper.unsafeParseJsonToJavaMap(header))
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)

      verified shouldBe Left(JwtVerifyError.DecodingError("DecodingFailure at .mapping: Missing required field", null))
    }

    "fail to decode a token with payload" in {
      val jwtVerifier = JwtVerifier(defaultConfig)
      val payload     = """{"name": "name"}"""
      val token = JWT
        .create()
        .withPayload(CodecHelper.unsafeParseJsonToJavaMap(payload))
        .sign(defaultConfig.algorithm)

      val verified = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)

      verified shouldBe Left(JwtVerifyError.DecodingError("DecodingFailure at .mapping: Missing required field", null))
    }

    "fail to decode a token with header & payload" in {
      val jwtVerifier = JwtVerifier(defaultConfig)
      val header      = """{"name": "name"}"""
      val token = JWT
        .create()
        .withHeader(CodecHelper.unsafeParseJsonToJavaMap(header))
        .sign(defaultConfig.algorithm)

      val verified =
        jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)

      verified shouldBe Left(JwtVerifyError.DecodingError("DecodingFailure at .mapping: Missing required field", null))
    }

//    "fail to decode a token with header if exception raised in decoder" in {
//      val jwtVerifier = JwtVerifier(defaultConfig)
//      val token = JWT
//        .create()
//        .sign(defaultConfig.algorithm)
//
//      val verified = jwtVerifier.verifyJwt[SimpleHeader](token.toTokenH)
//
//      verified.left.value shouldEqual JwtVerifyError.DecodingError("Boom", null)
//    }
//
//    "fail to decode a token with payload if exception raised in decoder" in {
//      val jwtVerifier = JwtVerifier(defaultConfig)
//      val token = JWT
//        .create()
//        .sign(defaultConfig.algorithm)
//
//      val verified = jwtVerifier.verifyJwt[SimplePayload](token.toTokenP)
//
//      verified.left.value shouldEqual JwtVerifyError.DecodingError("Boom", null)
//    }
//
//    "fail to decode a token with header & payload if exception raised in decoder" in {
//      val jwtVerifier = JwtVerifier(defaultConfig)
//      val token = JWT
//        .create()
//        .sign(defaultConfig.algorithm)
//
//      val verified =
//        jwtVerifier.verifyJwt[SimpleHeader, SimplePayload](token.toTokenHP)
//
//      verified.left.value shouldEqual JwtVerifyError.DecodingError("Boom", null)
//    }
//
//    "fail to verify token with VerificationError when provided with claims are not meet criteria" in {
//      val config      = defaultConfig.copy(providedWith = defaultConfig.providedWith.copy(issuerClaim = Some("issuer")))
//      val jwtVerifier = JwtVerifier(config)
//      val token = JWT
//        .create()
//        .sign(config.algorithm)
//
//      val verified = jwtVerifier.verifyJwt(token.toToken)
//
//      verified.left.value shouldEqual JwtVerifyError.VerificationError(
//        "JwtVerifier failed with JWTVerificationException",
//        Some(new JWTVerificationException("The Claim 'iss' is not present in the JWT.")),
//      )
//    }
//
//    "fail to verify token with IllegalArgument when null algorithm is provided" in forAll {
//      (config: JwtVerifierConfig) =>
//        val jwtVerifier = JwtVerifier(config.copy(algorithm = null))
//        val token = JWT
//          .create()
//          .sign(config.algorithm)
//
//        val verified = jwtVerifier.verifyJwt(token.toToken)
//
//        verified.left.value shouldBe JwtVerifyError.VerificationError(
//          "JwtVerifier failed with IllegalArgumentException",
//          Some(new IllegalArgumentException("The Algorithm cannot be null.")),
//        )
//    }
//
//    "fail to verify token with AlgorithmMismatch when jwt header algorithm doesn't match with verify" in forAll {
//      (config: JwtVerifierConfig) =>
//        val jwtVerifier = JwtVerifier(config.copy(algorithm = Algorithm.HMAC256("secret")))
//        val token = JWT
//          .create()
//          .sign(config.algorithm)
//
//        val verified = jwtVerifier.verifyJwt(token.toToken)
//
//        verified.left.value shouldEqual
//          JwtVerifyError
//            .VerificationError(
//              "JwtVerifier failed with verification error",
//              Some(new AlgorithmMismatchException("The Algorithm used to sign the JWT is not the one expected.")),
//            )
//    }
//
//    "fail to verify token with SignatureVerificationError when secrets provided are wrong" in forAll {
//      (config: JwtVerifierConfig) =>
//        val jwtVerifier = JwtVerifier(config.copy(algorithm = Algorithm.HMAC256("secret2")))
//        val algorithm   = Algorithm.HMAC256("secret1")
//        val token = JWT
//          .create()
//          .sign(algorithm)
//
//        val verified = jwtVerifier.verifyJwt(token.toToken)
//
//        verified.left.value shouldEqual
//          JwtVerifyError
//            .VerificationError(
//              "JwtVerifier failed with SignatureVerificationException",
//              null,
//            )
//    }
//
//    "fail to verify token with TokenExpired when JWT expires" in {
//      val jwtVerifier = JwtVerifier(defaultConfig)
//      val expiresAt   = getInstantNowSeconds.minusSeconds(1)
//      val token = JWT
//        .create()
//        .withExpiresAt(expiresAt)
//        .sign(defaultConfig.algorithm)
//
//      val verified = jwtVerifier.verifyJwt(token.toToken)
//
//      verified.left.value shouldEqual
//        JwtVerifyError
//          .VerificationError(s"The Token has expired on $expiresAt.", null)
//    }
//
//    "fail to verify an empty string token" in {
//      val jwtVerifier = JwtVerifier(defaultConfig)
//      val token       = ""
//      val verified    = jwtVerifier.verifyJwt(token.toToken)
//      val verifiedH   = jwtVerifier.verifyJwt[NestedHeader](token.toTokenH)
//      val verifiedP   = jwtVerifier.verifyJwt[NestedPayload](token.toTokenP)
//      val verifiedHP  = jwtVerifier.verifyJwt[NestedHeader, NestedPayload](token.toTokenHP)
//
//      verified.left.value shouldBe
//        JwtVerifyError
//          .VerificationError("JWT Token is empty.")
//
//      verifiedH.left.value shouldBe
//        JwtVerifyError
//          .VerificationError("JWT Token is empty.")
//
//      verifiedP.left.value shouldBe
//        JwtVerifyError
//          .VerificationError("JWT Token is empty.")
//
//      verifiedHP.left.value shouldBe
//        JwtVerifyError
//          .VerificationError("JWT Token is empty.")
//    }
  }
}

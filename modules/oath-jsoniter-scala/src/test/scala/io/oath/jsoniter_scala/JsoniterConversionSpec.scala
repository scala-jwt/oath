package io.oath.jsoniter_scala

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.oath.config.JwtIssuerConfig.RegisteredConfig
import io.oath.config.JwtVerifierConfig._
import io.oath.config.{JwtIssuerConfig, JwtVerifierConfig}
import io.oath.jwt.{JwtIssuer, JwtVerifier}
import io.oath.model.{JwtClaims, JwtVerifyError}
import io.oath.testkit.AnyWordSpecBase
import io.oath.utils.CodecUtils

import io.oath.syntax.TokenOps

class JsoniterConversionSpec extends AnyWordSpecBase with CodecUtils {

  val verifierConfig =
    JwtVerifierConfig(
      Algorithm.none(),
      None,
      ProvidedWithConfig(None, None, Nil),
      LeewayWindowConfig(None, None, None, None),
    )
  val issuerConfig =
    JwtIssuerConfig(
      Algorithm.none(),
      None,
      RegisteredConfig(None, None, Nil, includeJwtIdClaim = false, includeIssueAtClaim = false, None, None),
    )

  val jwtVerifier = new JwtVerifier(verifierConfig)
  val jwtIssuer   = new JwtIssuer(issuerConfig)

  "JsoniterConversion" should {

    "convert jsoniter codec to claims (encoders & decoders)" in {
      val bar    = Bar("bar", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(bar)).value
      val claims = jwtVerifier.verifyJwt[Bar](jwt.token.toTokenP).value

      claims.payload shouldBe bar
    }

    "convert jsoniter codec to claims decoder and get error" in {
      val fooJson = """{"name":"Hello","age":"not number"}"""
      val jwt = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(fooJson))
        .sign(Algorithm.none())
      val claims = jwtVerifier.verifyJwt[Bar](jwt.toTokenP)

      val decodingError: JwtVerifyError = claims.left.value
      decodingError.error should startWith("illegal number, offset: 0x00000016, buf:")
    }
  }
}

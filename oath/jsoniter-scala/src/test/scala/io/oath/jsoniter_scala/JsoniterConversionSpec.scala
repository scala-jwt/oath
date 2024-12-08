package io.oath.jsoniter_scala

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.oath.*
import io.oath.config.JwtIssuerConfig.RegisteredConfig
import io.oath.config.JwtVerifierConfig.*
import io.oath.config.{JwtIssuerConfig, JwtVerifierConfig}
import io.oath.json.ClaimsDecoder
import io.oath.syntax.*
import io.oath.syntax.all.*
import io.oath.testkit.CodecHelper.unsafeParseJsonToJavaMap
import io.oath.testkit.WordSpecBase

class JsoniterConversionSpec extends WordSpecBase {

  val verifierConfig =
    JwtVerifierConfig(
      Algorithm.none(),
      ProvidedWithConfig(None, None, Nil),
      LeewayWindowConfig(None, None, None, None),
    )
  val issuerConfig =
    JwtIssuerConfig(
      Algorithm.none(),
      RegisteredConfig(None, None, Nil, includeJwtIdClaim = false, includeIssueAtClaim = false, None, None),
    )

  val jwtVerifier = JwtVerifier(verifierConfig)
  val jwtIssuer   = JwtIssuer(issuerConfig)

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

      claims.left.value shouldBe a[JwtVerifyError.DecodingError]
    }

    "convert jsoniter decoder to claims decoder and get error when format is incorrect" in {
      val barJson = """{"name":,}"""

      summon[ClaimsDecoder[Bar]].decode(barJson).left.value shouldBe a[JwtVerifyError.DecodingError]
    }
  }
}

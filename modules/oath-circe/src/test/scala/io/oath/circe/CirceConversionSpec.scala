package io.oath.circe

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.oath.*
import io.oath.circe.conversion.given
import io.oath.config.JwtIssuerConfig.RegisteredConfig
import io.oath.config.JwtVerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import io.oath.config.*
import io.oath.json.ClaimsDecoder
import io.oath.syntax.*
import io.oath.testkit.AnyWordSpecBase
import io.oath.utils.CodecUtils
import org.typelevel.jawn.ParseException

class CirceConversionSpec extends AnyWordSpecBase, CodecUtils:

  val verifierConfig =
    JwtVerifierConfig(
      Algorithm.HMAC256("secret"),
      None,
      ProvidedWithConfig(None, None, Nil),
      LeewayWindowConfig(None, None, None, None),
    )

  val issuerConfig =
    JwtIssuerConfig(
      Algorithm.HMAC256("secret"),
      None,
      RegisteredConfig(None, None, Nil, includeJwtIdClaim = false, includeIssueAtClaim = false, None, None),
    )

  val jwtVerifier = new JwtVerifier(verifierConfig)
  val jwtIssuer   = new JwtIssuer(issuerConfig)

  "CirceConversion" should {
    "convert circe (encoders & decoders) to claims (encoders & decoders)" in {
      val bar    = Bar("bar", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(bar)).value
      val claims = jwtVerifier.verifyJwt[Bar](jwt.token.toTokenP).value

      claims.payload shouldBe bar
    }

    "convert circe (codec) to claims (encoders & decoders)" in {
      val foo    = Foo("foo", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(foo, RegisteredClaims.empty.copy(iss = Some("issuer")))).value
      val claims = jwtVerifier.verifyJwt[Foo](jwt.token.toTokenP).value

      claims.payload shouldBe foo
    }

    "convert circe decoder to claims decoder and get error" in {
      val fooJson = """{"name":"Hello","age":"not number"}"""
      val jwt = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(fooJson))
        .sign(Algorithm.HMAC256("secret"))
      val claims = jwtVerifier.verifyJwt[Foo](jwt.toTokenP)

      claims.left.value shouldBe JwtVerifyError.DecodingError("DecodingFailure at .age: Int", null)
    }

    "convert circe decoder to claims decoder and get error when format is incorrect" in {
      val fooJson = """{"name":,}"""

      summon[ClaimsDecoder[Foo]].decode(fooJson).left.value shouldEqual JwtVerifyError.DecodingError(
        "expected json value got ',}' (line 1, column 9)",
        ParseException("expected json value got ',}' (line 1, column 9)", 8, 1, 9),
      )
    }
  }

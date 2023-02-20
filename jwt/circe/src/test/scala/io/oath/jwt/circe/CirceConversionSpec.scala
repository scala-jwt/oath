package io.oath.jwt.circe

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.config.JwtIssuerConfig.RegisteredConfig
import io.oath.jwt.config.JwtVerifierConfig.{LeewayWindowConfig, ProvidedWithConfig}
import io.oath.jwt.config.{JwtIssuerConfig, JwtVerifierConfig}
import io.oath.jwt.model.{JwtClaims, JwtVerifyError, RegisteredClaims}
import io.oath.jwt.syntax._
import io.oath.jwt.testkit.AnyWordSpecBase
import io.oath.jwt.utils.CodecUtils
import io.oath.jwt.{JwtIssuer, JwtVerifier}

import scala.util.chaining.scalaUtilChainingOps

class CirceConversionSpec extends AnyWordSpecBase with CodecUtils {

  val verifierConfig =
    JwtVerifierConfig(Algorithm.HMAC256("secret"),
                      None,
                      ProvidedWithConfig(None, None, Nil),
                      LeewayWindowConfig(None, None, None, None))
  val issuerConfig =
    JwtIssuerConfig(
      Algorithm.HMAC256("secret"),
      None,
      RegisteredConfig(None, None, Nil, includeJwtIdClaim = false, includeIssueAtClaim = false, None, None))

  val jwtVerifier = new JwtVerifier(verifierConfig)
  val jwtIssuer   = new JwtIssuer(issuerConfig)

  "CirceConversion" should {

    "convert circe (encoders & decoders) to claims (encoders & decoders)" in {
      val bar    = Bar("bar", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(bar)).value
      val claims = jwtVerifier.verifyJwt[Bar](jwt.token.value.toTokenP).value

      claims.payload shouldBe bar
    }

    "convert circe (codec) to claims (encoders & decoders)" in {
      val foo    = Foo("foo", 10)
      val jwt    = jwtIssuer.issueJwt(JwtClaims.ClaimsP(foo, RegisteredClaims.empty.copy(iss = Some("issuer")))).value
      val claims = jwtVerifier.verifyJwt[Foo](jwt.token.value.toTokenP).value

      claims.payload shouldBe foo
    }

    "convert circe decoder to claims decoder and get error" in {
      val fooJson = """{"name":"Hello","age":"not number"}"""
      val jwt = JWT
        .create()
        .withPayload(unsafeParseJsonToJavaMap(fooJson))
        .sign(Algorithm.HMAC256("secret"))
        .pipe(NonEmptyString.unsafeFrom)
      val claims = jwtVerifier.verifyJwt[Foo](jwt.value.toTokenP)

      claims.left.value shouldBe JwtVerifyError.DecodingError("DecodingFailure at .age: Int", null)
    }
  }
}

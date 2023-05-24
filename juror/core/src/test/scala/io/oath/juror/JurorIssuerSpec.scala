package io.oath.juror

import io.oath.jwt.testkit.AnyWordSpecBase

class JurorIssuerSpec extends AnyWordSpecBase {

  "JurorIssuer" should {

    "create jwt token issuers" in {
      val jurorIssuer = JurorIssuer.createOrFail(JurorToken)

      val accessTokenIssuer: JwtIssuer[JurorToken.AccessToken.type]   = jurorIssuer.as(JurorToken.AccessToken)
      val refreshTokenIssuer: JwtIssuer[JurorToken.RefreshToken.type] = jurorIssuer.as(JurorToken.RefreshToken)
      val activationEmailTokenIssuer: JwtIssuer[JurorToken.ActivationEmailToken.type] =
        jurorIssuer.as(JurorToken.ActivationEmailToken)
      val forgotPasswordTokenIssuer: JwtIssuer[JurorToken.ForgotPasswordToken.type] =
        jurorIssuer.as(JurorToken.ForgotPasswordToken)

      accessTokenIssuer.issueJwt().value.claims.registered.iss shouldBe "access-token"
      refreshTokenIssuer.issueJwt().value.claims.registered.iss shouldBe "refresh-token"
      activationEmailTokenIssuer.issueJwt().value.claims.registered.iss shouldBe "activation-email-token"
      forgotPasswordTokenIssuer.issueJwt().value.claims.registered.iss shouldBe "forgot-password-token"
    }
  }
}

package io.oath

import io.oath.testkit.AnyWordSpecBase

class OathIssuerSpec extends AnyWordSpecBase {

  "OathIssuer" should {

    "create jwt token issuers" in {
      val oathIssuer = OathIssuer.createOrFail(OathToken)

      val accessTokenIssuer: JwtIssuer[OathToken.AccessToken.type]   = oathIssuer.as(OathToken.AccessToken)
      val refreshTokenIssuer: JwtIssuer[OathToken.RefreshToken.type] = oathIssuer.as(OathToken.RefreshToken)
      val activationEmailTokenIssuer: JwtIssuer[OathToken.ActivationEmailToken.type] =
        oathIssuer.as(OathToken.ActivationEmailToken)
      val forgotPasswordTokenIssuer: JwtIssuer[OathToken.ForgotPasswordToken.type] =
        oathIssuer.as(OathToken.ForgotPasswordToken)

      accessTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("access-token")
      refreshTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("refresh-token")
      activationEmailTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("activation-email-token")
      forgotPasswordTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("forgot-password-token")
    }
  }
}

package io.oath

import io.oath.test.*

class OathIssuerSpec extends AnyWordSpecBase {

  "OathIssuer" should {
    "create jwt token issuers" in {
      inline def oathIssuer = OathIssuer.createOrFail[OathToken]

      val accessTokenIssuer: JIssuer[OathToken.AccessToken.type]   = oathIssuer.as(OathToken.AccessToken)
      val refreshTokenIssuer: JIssuer[OathToken.RefreshToken.type] = oathIssuer.as(OathToken.RefreshToken)
      val activationEmailTokenIssuer: JIssuer[OathToken.ActivationEmailToken.type] =
        oathIssuer.as(OathToken.ActivationEmailToken)
      val forgotPasswordTokenIssuer: JIssuer[OathToken.ForgotPasswordToken.type] =
        oathIssuer.as(OathToken.ForgotPasswordToken)

      accessTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("access-token")
      refreshTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("refresh-token")
      activationEmailTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("activation-email-token")
      forgotPasswordTokenIssuer.issueJwt().value.claims.registered.iss shouldBe Some("forgot-password-token")
    }
  }
}

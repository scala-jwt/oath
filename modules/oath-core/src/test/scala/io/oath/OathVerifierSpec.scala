package io.oath

import io.oath.testkit.AnyWordSpecBase

import io.oath.syntax.TokenOps

class OathVerifierSpec extends AnyWordSpecBase {

  val oathIssuer = OathIssuer.createOrFail(OathToken)

  val accessTokenIssuer: JwtIssuer[OathToken.AccessToken.type]   = oathIssuer.as(OathToken.AccessToken)
  val refreshTokenIssuer: JwtIssuer[OathToken.RefreshToken.type] = oathIssuer.as(OathToken.RefreshToken)
  val activationEmailTokenIssuer: JwtIssuer[OathToken.ActivationEmailToken.type] =
    oathIssuer.as(OathToken.ActivationEmailToken)
  val forgotPasswordTokenIssuer: JwtIssuer[OathToken.ForgotPasswordToken.type] =
    oathIssuer.as(OathToken.ForgotPasswordToken)

  "OathVerifier" should {

    "create different token verifiers" in {
      val oathVerifier = OathVerifier.createOrFail(OathToken)

      val accessTokenVerifier: JwtVerifier[OathToken.AccessToken.type]   = oathVerifier.as(OathToken.AccessToken)
      val refreshTokenVerifier: JwtVerifier[OathToken.RefreshToken.type] = oathVerifier.as(OathToken.RefreshToken)
      val activationEmailTokenVerifier: JwtVerifier[OathToken.ActivationEmailToken.type] =
        oathVerifier.as(OathToken.ActivationEmailToken)
      val forgotPasswordTokenVerifier: JwtVerifier[OathToken.ForgotPasswordToken.type] =
        oathVerifier.as(OathToken.ForgotPasswordToken)

      val accessToken          = accessTokenIssuer.issueJwt().value.token
      val refreshToken         = refreshTokenIssuer.issueJwt().value.token
      val activationEmailToken = activationEmailTokenIssuer.issueJwt().value.token
      val forgotPasswordToken  = forgotPasswordTokenIssuer.issueJwt().value.token

      accessTokenVerifier.verifyJwt(accessToken.toToken).isRight shouldBe true
      refreshTokenVerifier.verifyJwt(refreshToken.toToken).isRight shouldBe true
      activationEmailTokenVerifier.verifyJwt(activationEmailToken.toToken).isRight shouldBe true
      forgotPasswordTokenVerifier.verifyJwt(forgotPasswordToken.toToken).isRight shouldBe true
    }
  }
}

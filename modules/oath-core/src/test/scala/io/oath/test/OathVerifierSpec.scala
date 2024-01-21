package io.oath.test

import io.oath.*
import io.oath.syntax.*
import io.oath.testkit.AnyWordSpecBase

class OathVerifierSpec extends AnyWordSpecBase:

  val oathIssuer = OathIssuer.createOrFail[OathToken]

  val accessTokenIssuer: JIssuer[OathToken.AccessToken.type]   = oathIssuer.as(OathToken.AccessToken)
  val refreshTokenIssuer: JIssuer[OathToken.RefreshToken.type] = oathIssuer.as(OathToken.RefreshToken)
  val activationEmailTokenIssuer: JIssuer[OathToken.ActivationEmailToken.type] =
    oathIssuer.as(OathToken.ActivationEmailToken)
  val forgotPasswordTokenIssuer: JIssuer[OathToken.ForgotPasswordToken.type] =
    oathIssuer.as(OathToken.ForgotPasswordToken)

  "OathVerifier" should:

    "create different token verifiers" in:
      val oathVerifier = OathVerifier.createOrFail[OathToken]

      val accessTokenVerifier: JVerifier[OathToken.AccessToken.type]   = oathVerifier.as(OathToken.AccessToken)
      val refreshTokenVerifier: JVerifier[OathToken.RefreshToken.type] = oathVerifier.as(OathToken.RefreshToken)
      val activationEmailTokenVerifier: JVerifier[OathToken.ActivationEmailToken.type] =
        oathVerifier.as(OathToken.ActivationEmailToken)
      val forgotPasswordTokenVerifier: JVerifier[OathToken.ForgotPasswordToken.type] =
        oathVerifier.as(OathToken.ForgotPasswordToken)

      val accessToken          = accessTokenIssuer.issueJwt().value.token
      val refreshToken         = refreshTokenIssuer.issueJwt().value.token
      val activationEmailToken = activationEmailTokenIssuer.issueJwt().value.token
      val forgotPasswordToken  = forgotPasswordTokenIssuer.issueJwt().value.token

      accessTokenVerifier.verifyJwt(accessToken.toToken).isRight shouldBe true
      refreshTokenVerifier.verifyJwt(refreshToken.toToken).isRight shouldBe true
      activationEmailTokenVerifier.verifyJwt(activationEmailToken.toToken).isRight shouldBe true
      forgotPasswordTokenVerifier.verifyJwt(forgotPasswordToken.toToken).isRight shouldBe true

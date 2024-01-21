package io.oath.test

import io.oath.*
import io.oath.syntax.*
import io.oath.testkit.AnyWordSpecBase

class OathManagerSpec extends AnyWordSpecBase:

  "OathManager" should:
    "create different token managers" in:
      val oathManager = OathManager.createOrFail[OathToken]

      val accessTokenManager: JManager[OathToken.AccessToken.type]   = oathManager.as(OathToken.AccessToken)
      val refreshTokenManager: JManager[OathToken.RefreshToken.type] = oathManager.as(OathToken.RefreshToken)
      val activationEmailTokenManager: JManager[OathToken.ActivationEmailToken.type] =
        oathManager.as(OathToken.ActivationEmailToken)
      val forgotPasswordTokenManager: JManager[OathToken.ForgotPasswordToken.type] =
        oathManager.as(OathToken.ForgotPasswordToken)

      val accessToken          = accessTokenManager.issueJwt().value.token
      val refreshToken         = refreshTokenManager.issueJwt().value.token
      val activationEmailToken = activationEmailTokenManager.issueJwt().value.token
      val forgotPasswordToken  = forgotPasswordTokenManager.issueJwt().value.token

      accessTokenManager.verifyJwt(accessToken.toToken).isRight shouldBe true
      refreshTokenManager.verifyJwt(refreshToken.toToken).isRight shouldBe true
      activationEmailTokenManager.verifyJwt(activationEmailToken.toToken).isRight shouldBe true
      forgotPasswordTokenManager.verifyJwt(forgotPasswordToken.toToken).isRight shouldBe true

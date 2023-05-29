package io.oath

import io.oath.testkit.AnyWordSpecBase

import cats.implicits.catsSyntaxOptionId
import io.oath.syntax.TokenOps

class OathManagerSpec extends AnyWordSpecBase {

  "OathManager" should {

    "create different token managers" in {
      val oathManager = OathManager.createOrFail(OathToken)
      "as".some

      val accessTokenManager: JwtManager[OathToken.AccessToken.type]   = oathManager.as(OathToken.AccessToken)
      val refreshTokenManager: JwtManager[OathToken.RefreshToken.type] = oathManager.as(OathToken.RefreshToken)
      val activationEmailTokenManager: JwtManager[OathToken.ActivationEmailToken.type] =
        oathManager.as(OathToken.ActivationEmailToken)
      val forgotPasswordTokenManager: JwtManager[OathToken.ForgotPasswordToken.type] =
        oathManager.as(OathToken.ForgotPasswordToken)

      val accessToken          = accessTokenManager.issueJwt().value.token
      val refreshToken         = refreshTokenManager.issueJwt().value.token
      val activationEmailToken = activationEmailTokenManager.issueJwt().value.token
      val forgotPasswordToken  = forgotPasswordTokenManager.issueJwt().value.token

      accessTokenManager.verifyJwt(accessToken.toToken).isRight shouldBe true
      refreshTokenManager.verifyJwt(refreshToken.toToken).isRight shouldBe true
      activationEmailTokenManager.verifyJwt(activationEmailToken.toToken).isRight shouldBe true
      forgotPasswordTokenManager.verifyJwt(forgotPasswordToken.toToken).isRight shouldBe true
    }
  }

}

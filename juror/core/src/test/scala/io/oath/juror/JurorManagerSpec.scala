package io.oath.juror

import io.oath.jwt.testkit.AnyWordSpecBase

import io.oath.jwt.syntax.TokenOps

class JurorManagerSpec extends AnyWordSpecBase {

  "JurorManager" should {

    "create different token managers" in {
      val jurorManager = JurorManager.createOrFail(JurorToken)

      val accessTokenManager: JwtManager[JurorToken.AccessToken.type]   = jurorManager.as(JurorToken.AccessToken)
      val refreshTokenManager: JwtManager[JurorToken.RefreshToken.type] = jurorManager.as(JurorToken.RefreshToken)
      val activationEmailTokenManager: JwtManager[JurorToken.ActivationEmailToken.type] =
        jurorManager.as(JurorToken.ActivationEmailToken)
      val forgotPasswordTokenManager: JwtManager[JurorToken.ForgotPasswordToken.type] =
        jurorManager.as(JurorToken.ForgotPasswordToken)

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

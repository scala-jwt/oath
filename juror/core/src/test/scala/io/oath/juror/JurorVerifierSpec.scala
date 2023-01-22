package io.oath.juror

import io.oath.jwt.testkit.AnyWordSpecBase

import io.oath.jwt.syntax.TokenOps

class JurorVerifierSpec extends AnyWordSpecBase {

  val jurorIssuer = JurorIssuer.createOrFail(JurorToken)

  val accessTokenIssuer: JwtIssuer[JurorToken.AccessToken.type]   = jurorIssuer.as(JurorToken.AccessToken)
  val refreshTokenIssuer: JwtIssuer[JurorToken.RefreshToken.type] = jurorIssuer.as(JurorToken.RefreshToken)
  val activationEmailTokenIssuer: JwtIssuer[JurorToken.ActivationEmailToken.type] =
    jurorIssuer.as(JurorToken.ActivationEmailToken)
  val forgotPasswordTokenIssuer: JwtIssuer[JurorToken.ForgotPasswordToken.type] =
    jurorIssuer.as(JurorToken.ForgotPasswordToken)

  "JurorVerifier" should {

    "create different token verifiers" in {
      val jurorVerifier = JurorVerifier.createOrFail(JurorToken)

      val accessTokenVerifier: JwtVerifier[JurorToken.AccessToken.type]   = jurorVerifier.as(JurorToken.AccessToken)
      val refreshTokenVerifier: JwtVerifier[JurorToken.RefreshToken.type] = jurorVerifier.as(JurorToken.RefreshToken)
      val activationEmailTokenVerifier: JwtVerifier[JurorToken.ActivationEmailToken.type] =
        jurorVerifier.as(JurorToken.ActivationEmailToken)
      val forgotPasswordTokenVerifier: JwtVerifier[JurorToken.ForgotPasswordToken.type] =
        jurorVerifier.as(JurorToken.ForgotPasswordToken)

      val accessToken          = accessTokenIssuer.issueJwt().value.token
      val refreshToken         = refreshTokenIssuer.issueJwt().value.token
      val activationEmailToken = activationEmailTokenIssuer.issueJwt().value.token
      val forgotPasswordToken  = forgotPasswordTokenIssuer.issueJwt().value.token

      accessTokenVerifier.verifyJwt(accessToken.value.toToken).isRight shouldBe true
      refreshTokenVerifier.verifyJwt(refreshToken.value.toToken).isRight shouldBe true
      activationEmailTokenVerifier.verifyJwt(activationEmailToken.value.toToken).isRight shouldBe true
      forgotPasswordTokenVerifier.verifyJwt(forgotPasswordToken.value.toToken).isRight shouldBe true
    }
  }
}

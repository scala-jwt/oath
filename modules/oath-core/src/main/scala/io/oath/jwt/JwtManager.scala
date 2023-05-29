package io.oath.jwt

import io.oath.config._
import io.oath.json.{ClaimsDecoder, ClaimsEncoder}
import io.oath.jwt.{JwtIssuer, JwtVerifier}
import io.oath.model._

final class JwtManager(config: JwtManagerConfig) {

  private val issuer: JwtIssuer     = new JwtIssuer(config.issuer)
  private val verifier: JwtVerifier = new JwtVerifier(config.verifier)

  def issueJwt(
      claims: JwtClaims.Claims = JwtClaims.Claims()
  ): Either[JwtIssueError, Jwt[JwtClaims.Claims]] = issuer.issueJwt(claims)

  def issueJwt[H](claims: JwtClaims.ClaimsH[H])(implicit
      claimsEncoder: ClaimsEncoder[H]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]] = issuer.issueJwt(claims)

  def issueJwt[P](claims: JwtClaims.ClaimsP[P])(implicit
      claimsEncoder: ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]] =
    issuer.issueJwt(claims)

  def issueJwt[H, P](claims: JwtClaims.ClaimsHP[H, P])(implicit
      headerClaimsEncoder: ClaimsEncoder[H],
      payloadClaimsEncoder: ClaimsEncoder[P],
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsHP[H, P]]] = issuer.issueJwt(claims)

  def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims] = verifier.verifyJwt(jwt)

  def verifyJwt[H](jwt: JwtToken.TokenH)(implicit
      claimsDecoder: ClaimsDecoder[H]
  ): Either[JwtVerifyError, JwtClaims.ClaimsH[H]] = verifier.verifyJwt(jwt)

  def verifyJwt[P](jwt: JwtToken.TokenP)(implicit
      claimsDecoder: ClaimsDecoder[P]
  ): Either[JwtVerifyError, JwtClaims.ClaimsP[P]] = verifier.verifyJwt(jwt)

  def verifyJwt[H, P](jwt: JwtToken.TokenHP)(implicit
      headerDecoder: ClaimsDecoder[H],
      payloadDecoder: ClaimsDecoder[P],
  ): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]] = verifier.verifyJwt(jwt)(headerDecoder, payloadDecoder)
}

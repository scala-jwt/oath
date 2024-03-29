package io.oath

import io.oath.config.*
import io.oath.json.{ClaimsDecoder, ClaimsEncoder}

final class JwtManager(config: JwtManagerConfig):

  private val issuer: JwtIssuer     = new JwtIssuer(config.issuer)
  private val verifier: JwtVerifier = new JwtVerifier(config.verifier)

  def issueJwt(
      claims: JwtClaims.Claims = JwtClaims.Claims()
  ): Either[JwtIssueError, Jwt[JwtClaims.Claims]] = issuer.issueJwt(claims)

  def issueJwt[H](claims: JwtClaims.ClaimsH[H])(using
      ClaimsEncoder[H]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]] = issuer.issueJwt(claims)

  def issueJwt[P](claims: JwtClaims.ClaimsP[P])(using
      ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]] =
    issuer.issueJwt(claims)

  def issueJwt[H, P](
      claims: JwtClaims.ClaimsHP[H, P]
  )(using ClaimsEncoder[H], ClaimsEncoder[P]): Either[JwtIssueError, Jwt[JwtClaims.ClaimsHP[H, P]]] =
    issuer.issueJwt(claims)

  def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims] = verifier.verifyJwt(jwt)

  def verifyJwt[H](jwt: JwtToken.TokenH)(using ClaimsDecoder[H]): Either[JwtVerifyError, JwtClaims.ClaimsH[H]] =
    verifier.verifyJwt(jwt)

  def verifyJwt[P](jwt: JwtToken.TokenP)(using ClaimsDecoder[P]): Either[JwtVerifyError, JwtClaims.ClaimsP[P]] =
    verifier.verifyJwt(jwt)

  def verifyJwt[H, P](
      jwt: JwtToken.TokenHP
  )(using ClaimsDecoder[H], ClaimsDecoder[P]): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]] =
    verifier.verifyJwt[H, P](jwt)

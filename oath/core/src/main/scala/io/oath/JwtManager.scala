package io.oath

import io.oath.config.*
import io.oath.json.{ClaimsDecoder, ClaimsEncoder}

import java.time.Clock

trait JwtManager {
  def issueJwt(claims: JwtClaims.Claims = JwtClaims.Claims()): Either[JwtIssueError, Jwt[JwtClaims.Claims]]
  def issueJwt[H](claims: JwtClaims.ClaimsH[H])(using
      ClaimsEncoder[H]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsH[H]]]
  def issueJwt[P](claims: JwtClaims.ClaimsP[P])(using
      ClaimsEncoder[P]
  ): Either[JwtIssueError, Jwt[JwtClaims.ClaimsP[P]]]
  def issueJwt[H, P](
      claims: JwtClaims.ClaimsHP[H, P]
  )(using ClaimsEncoder[H], ClaimsEncoder[P]): Either[JwtIssueError, Jwt[JwtClaims.ClaimsHP[H, P]]]
  def verifyJwt(jwt: JwtToken.Token): Either[JwtVerifyError, JwtClaims.Claims]
  def verifyJwt[H](jwt: JwtToken.TokenH)(using ClaimsDecoder[H]): Either[JwtVerifyError, JwtClaims.ClaimsH[H]]
  def verifyJwt[P](jwt: JwtToken.TokenP)(using ClaimsDecoder[P]): Either[JwtVerifyError, JwtClaims.ClaimsP[P]]
  def verifyJwt[H, P](
      jwt: JwtToken.TokenHP
  )(using ClaimsDecoder[H], ClaimsDecoder[P]): Either[JwtVerifyError, JwtClaims.ClaimsHP[H, P]]
}

object JwtManager {
  private final class JavaJwtManagerImpl(config: JwtManagerConfig, clock: Clock) extends JwtManager {

    private val issuer   = JwtIssuer(config.issuer, clock)
    private val verifier = JwtVerifier(config.verifier)

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
  }

  def apply(config: JwtManagerConfig, clock: Clock = Clock.systemUTC()): JwtManager =
    new JavaJwtManagerImpl(config, clock)
}

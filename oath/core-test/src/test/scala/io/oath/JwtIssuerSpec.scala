package io.oath

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.oath.config.*
import io.oath.syntax.all.*
import io.oath.test.NestedHeader.nestedHeaderDecoder
import io.oath.test.NestedPayload.nestedPayloadDecoder
import io.oath.test.*
import io.oath.utils.*

import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

class JwtIssuerSpec extends AnyWordSpecBase, PropertyBasedTesting, ClockHelper {

  val jwtVerifier = JWT
    .require(Algorithm.none())
    .acceptLeeway(5)
    .build()

  "JwtIssuer" should {
    "issue jwt tokens" when {
      "issue token with predefine configure claims" in forAll { (config: JwtIssuerConfig) =>
        val now       = getInstantNowSeconds
        val jwtIssuer = new JwtIssuer(config.copy(encrypt = None), getFixedClock(now))
        val jwtClaims = jwtIssuer.issueJwt().value

        val decodedJWT = jwtVerifier.verify(jwtClaims.token)

        Option(decodedJWT.getIssuer) shouldBe config.registered.issuerClaim
        Option(decodedJWT.getSubject) shouldBe config.registered.subjectClaim
        Option(decodedJWT.getAudience)
          .map(_.asScala.toSeq)
          .toSeq
          .flatten shouldBe config.registered.audienceClaims

        Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe Option.when(config.registered.includeIssueAtClaim)(now)

        if (config.registered.includeJwtIdClaim)
          Option(decodedJWT.getId) should not be empty
        else
          Option(decodedJWT.getId) shouldBe empty

        Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe config.registered.expiresAtOffset.map(offset =>
          now.plusSeconds(offset.toSeconds)
        )

        Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe config.registered.notBeforeOffset.map(offset =>
          now.plusSeconds(offset.toSeconds)
        )
      }

      "issue token with predefine configure claims and ad-hoc registered claims" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          val now       = getInstantNowSeconds
          val jwtIssuer = new JwtIssuer(config, getFixedClock(now))
          val jwtClaims = jwtIssuer.issueJwt(registeredClaims.toClaims).value

          val expectedIssuer  = registeredClaims.iss orElse config.registered.issuerClaim
          val expectedSubject = registeredClaims.sub orElse config.registered.subjectClaim
          val expectedAudience =
            if (registeredClaims.aud.nonEmpty) registeredClaims.aud else config.registered.audienceClaims
          val expectedIssuedAt = registeredClaims.iat orElse Option.when(config.registered.includeIssueAtClaim)(now)
          val expectedExpiredAt =
            registeredClaims.exp orElse config.registered.expiresAtOffset.map(offset =>
              now.plusSeconds(offset.toSeconds)
            )
          val expectedNotBefore =
            registeredClaims.nbf orElse config.registered.notBeforeOffset.map(offset =>
              now.plusSeconds(offset.toSeconds)
            )

          jwtClaims.claims.registered.iss shouldBe expectedIssuer
          jwtClaims.claims.registered.sub shouldBe expectedSubject
          jwtClaims.claims.registered.aud shouldBe expectedAudience
          jwtClaims.claims.registered.iat shouldBe expectedIssuedAt
          jwtClaims.claims.registered.exp shouldBe expectedExpiredAt
          jwtClaims.claims.registered.nbf shouldBe expectedNotBefore

          if (registeredClaims.jti.nonEmpty)
            jwtClaims.claims.registered.jti shouldBe registeredClaims.jti
          else if (config.registered.includeJwtIdClaim)
            jwtClaims.claims.registered.jti should not be empty
          else jwtClaims.claims.registered.jti shouldBe empty
      }

      "issue token with only registered claims empty strings" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          val now = getInstantNowSeconds
          val adHocRegisteredClaims =
            registeredClaims.copy(iat = Some(now), exp = Some(now.plusSeconds(5.minutes.toSeconds)), nbf = Some(now))
          val jwtIssuer = new JwtIssuer(config.copy(encrypt = None), getFixedClock(now))
          val jwtClaims = jwtIssuer.issueJwt(adHocRegisteredClaims.toClaims).value

          val decodedJWT = jwtVerifier.verify(jwtClaims.token)

          Option(decodedJWT.getIssuer) shouldBe jwtClaims.claims.registered.iss
          Option(decodedJWT.getSubject) shouldBe jwtClaims.claims.registered.sub
          Option(decodedJWT.getAudience)
            .map(_.asScala.toSeq)
            .toSeq
            .flatten shouldBe jwtClaims.claims.registered.aud
          Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe jwtClaims.claims.registered.iat
          Option(decodedJWT.getId) shouldBe jwtClaims.claims.registered.jti
          Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe jwtClaims.claims.registered.exp
          Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe jwtClaims.claims.registered.nbf
      }

      "issue token with only registered claims when decoded should have the same values with the return registered claims" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          val now = getInstantNowSeconds
          val adHocRegisteredClaims =
            registeredClaims.copy(iat = Some(now), exp = Some(now.plusSeconds(5.minutes.toSeconds)), nbf = Some(now))
          val jwtIssuer = new JwtIssuer(config.copy(encrypt = None), getFixedClock(now))
          val jwtClaims = jwtIssuer.issueJwt(adHocRegisteredClaims.toClaims).value

          val decodedJWT = jwtVerifier.verify(jwtClaims.token)

          Option(decodedJWT.getIssuer) shouldBe jwtClaims.claims.registered.iss
          Option(decodedJWT.getSubject) shouldBe jwtClaims.claims.registered.sub
          Option(decodedJWT.getAudience)
            .map(_.asScala.toSeq)
            .toSeq
            .flatten shouldBe jwtClaims.claims.registered.aud
          Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe jwtClaims.claims.registered.iat
          Option(decodedJWT.getId) shouldBe jwtClaims.claims.registered.jti
          Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe jwtClaims.claims.registered.exp
          Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe jwtClaims.claims.registered.nbf
      }

      "issue token with only registered claims encrypted" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          whenever(config.encrypt.nonEmpty):
            val clock     = getFixedClock(getInstantNowSeconds)
            val jwtIssuer = new JwtIssuer(config, clock)
            val jwt       = jwtIssuer.issueJwt(registeredClaims.toClaims).value

            jwt.token should fullyMatch regex """[0123456789ABCDEF]+"""
            jwt.token.length % 16 shouldBe 0
      }

      "issue token with header claims" in forAll { (config: JwtIssuerConfig, header: NestedHeader) =>
        val jwtIssuer = new JwtIssuer(config.copy(encrypt = None))
        val jwt       = jwtIssuer.issueJwt(header.toClaimsH).value

        val result = jwtVerifier
          .verify(jwt.token)
          .pipe(_.getHeader)
          .pipe(base64DecodeToken)
          .pipe(_.value)
          .pipe(nestedHeaderDecoder.decode)
          .value

        result shouldBe header
      }

      "issue token with header claims encrypted" in forAll { (config: JwtIssuerConfig, header: NestedHeader) =>
        whenever(config.encrypt.nonEmpty):
          val jwtIssuer = new JwtIssuer(config)
          val jwt       = jwtIssuer.issueJwt(header.toClaimsH).value

          jwt.token should fullyMatch regex """[0123456789ABCDEF]+"""
          jwt.token.length % 16 shouldBe 0
      }

      "issue token with payload claims" in forAll { (config: JwtIssuerConfig, payload: NestedPayload) =>
        val jwtIssuer = new JwtIssuer(config.copy(encrypt = None))
        val jwt       = jwtIssuer.issueJwt(payload.toClaimsP).value

        val result = jwtVerifier
          .verify(jwt.token)
          .pipe(_.getPayload)
          .pipe(base64DecodeToken)
          .pipe(_.value)
          .pipe(nestedPayloadDecoder.decode)
          .value

        result shouldBe payload
      }

      "issue token with payload claims encrypted" in forAll { (config: JwtIssuerConfig, payload: NestedPayload) =>
        whenever(config.encrypt.nonEmpty):
          val jwtIssuer = new JwtIssuer(config)
          val jwt       = jwtIssuer.issueJwt(payload.toClaimsP).value

          jwt.token should fullyMatch regex """[0123456789ABCDEF]+"""
          jwt.token.length % 16 shouldBe 0
      }

      "issue token with header & payload claims" in forAll {
        (config: JwtIssuerConfig, header: NestedHeader, payload: NestedPayload) =>
          val jwtIssuer = new JwtIssuer(config.copy(encrypt = None))
          val jwt       = jwtIssuer.issueJwt((header, payload).toClaimsHP).value

          val (headerResult, payloadResult) = jwtVerifier
            .verify(jwt.token)
            .pipe(decodedJwt =>
              base64DecodeToken(decodedJwt.getHeader).value -> base64DecodeToken(decodedJwt.getPayload).value
            )
            .pipe { case (headerJson, payloadJson) =>
              (nestedHeaderDecoder.decode(headerJson).value, nestedPayloadDecoder.decode(payloadJson).value)
            }

          headerResult shouldBe header
          payloadResult shouldBe payload
      }

      "issue token with header & payload claims encrypted" in forAll {
        (config: JwtIssuerConfig, header: NestedHeader, payload: NestedPayload) =>
          whenever(config.encrypt.nonEmpty):
            val jwtIssuer = new JwtIssuer(config)
            val jwt       = jwtIssuer.issueJwt((header, payload).toClaimsHP).value

            jwt.token should fullyMatch regex """[0123456789ABCDEF]+"""
            jwt.token.length % 16 shouldBe 0
      }

      "issue token should fail with IllegalArgument when algorithm is set to null" in forAll {
        (config: JwtIssuerConfig) =>
          val jwtIssuer = new JwtIssuer(config.copy(algorithm = null))
          val jwt       = jwtIssuer.issueJwt()

          jwt.left.value shouldEqual JwtIssueError.IllegalArgument(
            "JwtIssuer failed with IllegalArgumentException",
            new IllegalArgumentException("Algorithm cannot be null"),
          )
      }
    }
  }
}

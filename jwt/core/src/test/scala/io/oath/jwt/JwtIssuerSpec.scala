package io.oath.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eu.timepit.refined.types.string.NonEmptyString
import io.oath.jwt.NestedHeader._
import io.oath.jwt.NestedPayload._
import io.oath.jwt.config.JwtIssuerConfig
import io.oath.jwt.model.{JwtIssueError, RegisteredClaims}
import io.oath.jwt.syntax._
import io.oath.jwt.testkit.{AnyWordSpecBase, PropertyBasedTesting}
import io.oath.jwt.utils._

import scala.util.Try

import cats.implicits.catsSyntaxEitherId
import cats.implicits.catsSyntaxOptionId
import cats.implicits.toTraverseOps
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.chaining.scalaUtilChainingOps

class JwtIssuerSpec extends AnyWordSpecBase with PropertyBasedTesting with ClockHelper {

  val jwtVerifier = JWT
    .require(Algorithm.none())
    .acceptLeeway(1)
    .build()

  "JwtIssuer" should {

    "issue jwt tokens" when {

      "issue token with predefine configure claims" in forAll { config: JwtIssuerConfig =>
        val (time, clock) = timeWithClock
        val jwtIssuer = new JwtIssuer(config.copy(encrypt = None), clock)
        val jwtClaims = jwtIssuer.issueJwt().value

        val decodedJWT = jwtVerifier.verify(jwtClaims.token.value)

        Option(decodedJWT.getIssuer).flatMap(NonEmptyString.unapply) shouldBe config.registered.issuerClaim
        Option(decodedJWT.getSubject).flatMap(NonEmptyString.unapply) shouldBe config.registered.subjectClaim
        Option(decodedJWT.getAudience)
          .map(_.asScala.toSeq)
          .sequence
          .flatten
          .flatMap(NonEmptyString.unapply) shouldBe config.registered.audienceClaims

        Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe Option.when(config.registered.includeIssueAtClaim)(time)

        if (config.registered.includeJwtIdClaim)
          Option(decodedJWT.getId) should not be empty
        else
          Option(decodedJWT.getId) shouldBe empty

        Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe config.registered.expiresAtOffset.map(offset =>
          time.plusSeconds(offset.toSeconds))

        Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe config.registered.notBeforeOffset.map(offset =>
          time.plusSeconds(offset.toSeconds))
      }

      "issue token with predefine configure claims and ad-hoc registered claims" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          val (time, clock) = timeWithClock
          val jwtIssuer = new JwtIssuer(config, clock)
          val jwtClaims = jwtIssuer.issueJwt(registeredClaims.toClaims).value

          val expectedIssuer  = registeredClaims.iss orElse config.registered.issuerClaim
          val expectedSubject = registeredClaims.sub orElse config.registered.subjectClaim
          val expectedAudience =
            if (registeredClaims.aud.nonEmpty) registeredClaims.aud else config.registered.audienceClaims
          val expectedIssuedAt = registeredClaims.iat orElse Option.when(config.registered.includeIssueAtClaim)(time)
          val expectedExpiredAt =
            registeredClaims.exp orElse config.registered.expiresAtOffset.map(offset =>
              time.plusSeconds(offset.toSeconds))
          val expectedNotBefore =
            registeredClaims.nbf orElse config.registered.notBeforeOffset.map(offset =>
              time.plusSeconds(offset.toSeconds))

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

      "issue token with registered claims when decoded should have the same values with the return registered claims" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          val (time, clock) = timeWithClock
          val adHocRegisteredClaims =
            registeredClaims.copy(iat = time.some, exp = time.plusSeconds(5.minutes.toSeconds).some, nbf = time.some)
          val jwtIssuer = new JwtIssuer(config.copy(encrypt = None), clock)
          val jwtClaims = jwtIssuer.issueJwt(adHocRegisteredClaims.toClaims).value

          val decodedJWT = jwtVerifier.verify(jwtClaims.token.value)

          Option(decodedJWT.getIssuer).flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.iss
          Option(decodedJWT.getSubject).flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.sub
          Option(decodedJWT.getAudience)
            .map(_.asScala.toSeq)
            .sequence
            .flatten
            .flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.aud
          Try(decodedJWT.getIssuedAt.toInstant).toOption shouldBe jwtClaims.claims.registered.iat
          Option(decodedJWT.getId).flatMap(NonEmptyString.unapply) shouldBe jwtClaims.claims.registered.jti
          Try(decodedJWT.getExpiresAt.toInstant).toOption shouldBe jwtClaims.claims.registered.exp
          Try(decodedJWT.getNotBefore.toInstant).toOption shouldBe jwtClaims.claims.registered.nbf
      }

      "issue token with only registered claims encrypted" in forAll {
        (registeredClaims: RegisteredClaims, config: JwtIssuerConfig) =>
          whenever(config.encrypt.nonEmpty) {
            val (_, clock) = timeWithClock
            val jwtIssuer = new JwtIssuer(config, clock)
            val jwt       = jwtIssuer.issueJwt(registeredClaims.toClaims).value

            jwt.token.value should fullyMatch regex """[0123456789ABCDEF]+"""
            jwt.token.value.length % 16 shouldBe 0
          }
      }

      "issue token with header claims" in forAll { (config: JwtIssuerConfig, header: NestedHeader) =>
        val jwtIssuer = new JwtIssuer(config.copy(encrypt = None))
        val jwt       = jwtIssuer.issueJwt(header.toClaimsH).value

        val result = jwtVerifier
          .verify(jwt.token.value)
          .pipe(_.getHeader)
          .pipe(base64DecodeToken)
          .pipe(_.value)
          .pipe(nestedHeaderDecoder.decode)
          .value

        result shouldBe header
      }

      "issue token with header claims encrypted" in forAll { (config: JwtIssuerConfig, header: NestedHeader) =>
        whenever(config.encrypt.nonEmpty) {
          val jwtIssuer = new JwtIssuer(config)
          val jwt       = jwtIssuer.issueJwt(header.toClaimsH).value

          jwt.token.value should fullyMatch regex """[0123456789ABCDEF]+"""
          jwt.token.value.length % 16 shouldBe 0
        }
      }

      "issue token with payload claims" in forAll { (config: JwtIssuerConfig, payload: NestedPayload) =>
        val jwtIssuer = new JwtIssuer(config.copy(encrypt = None))
        val jwt       = jwtIssuer.issueJwt(payload.toClaimsP).value

        val result = jwtVerifier
          .verify(jwt.token.value)
          .pipe(_.getPayload)
          .pipe(base64DecodeToken)
          .pipe(_.value)
          .pipe(nestedPayloadDecoder.decode)
          .value

        result shouldBe payload
      }

      "issue token with payload claims encrypted" in forAll { (config: JwtIssuerConfig, payload: NestedPayload) =>
        whenever(config.encrypt.nonEmpty) {
          val jwtIssuer = new JwtIssuer(config)
          val jwt       = jwtIssuer.issueJwt(payload.toClaimsP).value

          jwt.token.value should fullyMatch regex """[0123456789ABCDEF]+"""
          jwt.token.value.length % 16 shouldBe 0
        }
      }

      "issue token with header & payload claims" in forAll {
        (config: JwtIssuerConfig, header: NestedHeader, payload: NestedPayload) =>
          val jwtIssuer = new JwtIssuer(config.copy(encrypt = None))
          val jwt       = jwtIssuer.issueJwt((header, payload).toClaimsHP).value

          val (headerResult, payloadResult) = jwtVerifier
            .verify(jwt.token.value)
            .pipe(decodedJwt =>
              base64DecodeToken(decodedJwt.getHeader).value -> base64DecodeToken(decodedJwt.getPayload).value)
            .pipe { case (headerJson, payloadJson) =>
              (nestedHeaderDecoder.decode(headerJson).value, nestedPayloadDecoder.decode(payloadJson).value)
            }

          headerResult shouldBe header
          payloadResult shouldBe payload

      }

      "issue token with header & payload claims encrypted" in forAll {
        (config: JwtIssuerConfig, header: NestedHeader, payload: NestedPayload) =>
          whenever(config.encrypt.nonEmpty) {
            val jwtIssuer = new JwtIssuer(config)
            val jwt       = jwtIssuer.issueJwt((header, payload).toClaimsHP).value

            jwt.token.value should fullyMatch regex """[0123456789ABCDEF]+"""
            jwt.token.value.length % 16 shouldBe 0
          }
      }

      "issue token should fail with IllegalArgument when algorithm is set to null" in forAll {
        config: JwtIssuerConfig =>
          val jwtIssuer = new JwtIssuer(config.copy(algorithm = null))
          val jwt       = jwtIssuer.issueJwt()

          jwt shouldBe JwtIssueError.IllegalArgument("The Algorithm cannot be null.").asLeft
      }
    }
  }
}

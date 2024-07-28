package io.oath

import io.oath.config.*
import io.oath.syntax.all.*
import io.oath.test.*

class JwtManagerSpec extends AnyWordSpecBase, PropertyBasedTesting {

  "JwtManager" should {
    "be able to issue and verify jwt tokens without claims" in forAll { (config: JwtManagerConfig) =>
      val jwtManager = new JwtManager(config)

      val jwt = jwtManager.issueJwt().value
      jwtManager.verifyJwt(jwt.token.toToken).value.registered shouldBe jwt.claims.registered
    }

    "be able to issue and verify jwt tokens with header claims" in forAll {
      (config: JwtManagerConfig, nestedHeader: NestedHeader) =>
        val jwtManager = new JwtManager(config)

        val claims = nestedHeader.toClaimsH
        val jwt    = jwtManager.issueJwt(claims).value
        jwtManager.verifyJwt[NestedHeader](jwt.token.toTokenH).value shouldBe claims
          .copy(registered = jwt.claims.registered)
    }

    "be able to issue and verify jwt tokens with payload claims" in forAll {
      (config: JwtManagerConfig, nestedPayload: NestedPayload) =>
        val jwtManager = new JwtManager(config)

        val claims = nestedPayload.toClaimsP
        val jwt    = jwtManager.issueJwt(claims).value
        jwtManager.verifyJwt[NestedPayload](jwt.token.toTokenP).value shouldBe claims
          .copy(registered = jwt.claims.registered)
    }

    "be able to issue and verify jwt tokens with header & payload claims" in forAll {
      (config: JwtManagerConfig, nestedHeader: NestedHeader, nestedPayload: NestedPayload) =>
        val jwtManager = new JwtManager(config)

        val claims = (nestedHeader, nestedPayload).toClaimsHP
        val jwt    = jwtManager.issueJwt(claims).value
        jwtManager.verifyJwt[NestedHeader, NestedPayload](jwt.token.toTokenHP).value shouldBe claims
          .copy(registered = jwt.claims.registered)
    }
  }
}

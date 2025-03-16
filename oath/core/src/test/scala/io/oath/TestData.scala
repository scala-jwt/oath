package io.oath

import com.auth0.jwt.JWTCreator

final case class TestData(registeredClaims: RegisteredClaims, builder: JWTCreator.Builder)

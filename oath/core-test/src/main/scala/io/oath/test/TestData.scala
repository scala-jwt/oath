package io.oath.test

import com.auth0.jwt.JWTCreator
import io.oath.RegisteredClaims

case class TestData(registeredClaims: RegisteredClaims, builder: JWTCreator.Builder)

package io.oath.jwt.utils

import com.auth0.jwt.JWTCreator
import io.oath.jwt.model.RegisteredClaims

final case class TestData(registeredClaims: RegisteredClaims, builder: JWTCreator.Builder)

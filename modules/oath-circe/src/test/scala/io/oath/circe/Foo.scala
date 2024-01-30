package io.oath.circe

import io.circe.Codec
import io.circe.generic.semiauto.*
import io.oath.circe.syntax.*
import io.oath.json.ClaimsCodec

final case class Foo(name: String, age: Int)

object Foo:
  given barCodec: ClaimsCodec[Foo] = deriveCodec[Foo].convertCodec

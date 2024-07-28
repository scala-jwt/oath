package io.oath.circe

import io.circe.Codec
import io.circe.generic.semiauto.*

final case class Foo(name: String, age: Int)

object Foo:
  given barCodec: Codec[Foo] = deriveCodec[Foo]

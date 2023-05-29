package io.oath.circe

import io.circe.Codec
import io.circe.generic.semiauto._

final case class Foo(name: String, age: Int)

object Foo {

  implicit val barCodec: Codec[Foo] = deriveCodec[Foo]
}

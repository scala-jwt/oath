package io.oath.circe

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

final case class Bar(name: String, age: Int)

object Bar {

  implicit val barEncoder: Encoder[Bar] = deriveEncoder[Bar]
  implicit val barDecoder: Decoder[Bar] = deriveDecoder[Bar]
}

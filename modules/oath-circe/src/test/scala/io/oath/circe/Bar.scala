package io.oath.circe

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

case class Bar(name: String, age: Int)

object Bar:
  given barEncoder: Encoder[Bar] = deriveEncoder[Bar]
  given barDecoder: Decoder[Bar] = deriveDecoder[Bar]

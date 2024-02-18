package io.oath.circe

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import io.oath.circe.syntax.*
import io.oath.json.*

case class Bar(name: String, age: Int)

object Bar:
  given barEncoder: ClaimsEncoder[Bar] = deriveEncoder[Bar].convert
  given barDecoder: ClaimsDecoder[Bar] = deriveDecoder[Bar].convert

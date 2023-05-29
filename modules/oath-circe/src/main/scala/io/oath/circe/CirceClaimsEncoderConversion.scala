package io.oath.circe

import io.circe.Encoder
import io.oath.json.ClaimsEncoder

import io.circe.syntax.EncoderOps

trait CirceClaimsEncoderConversion {

  implicit def encoderConversion[A](implicit encoder: Encoder[A]): ClaimsEncoder[A] = data => data.asJson.noSpaces
}

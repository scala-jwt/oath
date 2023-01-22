package io.oath.jwt.circe

import io.circe.Encoder
import io.oath.jwt.ClaimsEncoder

import io.circe.syntax.EncoderOps

trait CirceClaimsEncoderConversion {

  implicit def encoderConversion[A](implicit encoder: Encoder[A]): ClaimsEncoder[A] = data => data.asJson.noSpaces
}

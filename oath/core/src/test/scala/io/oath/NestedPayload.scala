package io.oath

import io.circe.generic.semiauto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import io.oath.NestedPayload.SimplePayload
import io.oath.json.*

final case class NestedPayload(name: String, mapping: Map[String, SimplePayload])

object NestedPayload {
  final case class SimplePayload(name: String, data: List[String])

  given Encoder[SimplePayload] = deriveEncoder[SimplePayload]

  given Decoder[SimplePayload] = deriveDecoder[SimplePayload]

  given circeEncoder: Encoder[NestedPayload] = deriveEncoder[NestedPayload]

  given circeDecoder: Decoder[NestedPayload] = deriveDecoder[NestedPayload]

  given ClaimsEncoder[SimplePayload] = simplePayload => simplePayload.asJson.noSpaces

  given ClaimsDecoder[SimplePayload] = _ => throw new RuntimeException("Boom")

  given claimsEncoder: ClaimsEncoder[NestedPayload] = nestedPayload => nestedPayload.asJson.noSpaces

  given claimsDecoder: ClaimsDecoder[NestedPayload] = nestedPayloadJson =>
    parse(nestedPayloadJson).left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[NestedPayload].left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )
}

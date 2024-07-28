package io.oath.test

import io.circe.generic.semiauto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import io.oath.*
import io.oath.json.*
import io.oath.test.NestedPayload.SimplePayload

final case class NestedPayload(name: String, mapping: Map[String, SimplePayload])

object NestedPayload:
  final case class SimplePayload(name: String, data: List[String])

  given simplePayloadCirceEncoder: Encoder[SimplePayload] = deriveEncoder[SimplePayload]
  given simplePayloadCirceDecoder: Decoder[SimplePayload] = deriveDecoder[SimplePayload]

  given nestedPayloadCirceEncoder: Encoder[NestedPayload] = deriveEncoder[NestedPayload]
  given nestedPayloadCirceDecoder: Decoder[NestedPayload] = deriveDecoder[NestedPayload]

  given simplePayloadEncoder: ClaimsEncoder[SimplePayload] = simplePayload => simplePayload.asJson.noSpaces
  given simplePayloadDecoder: ClaimsDecoder[SimplePayload] = _ => throw new RuntimeException("Boom")

  given nestedPayloadEncoder: ClaimsEncoder[NestedPayload] = nestedPayload => nestedPayload.asJson.noSpaces
  given nestedPayloadDecoder: ClaimsDecoder[NestedPayload] = nestedPayloadJson =>
    parse(nestedPayloadJson).left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[NestedPayload].left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )

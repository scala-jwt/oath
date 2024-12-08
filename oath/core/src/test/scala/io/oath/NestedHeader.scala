package io.oath

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.oath.NestedHeader.SimpleHeader
import io.oath.json.*

final case class NestedHeader(name: String, mapping: Map[String, SimpleHeader])

object NestedHeader {
  final case class SimpleHeader(name: String, data: List[String])

  given Encoder[SimpleHeader] = deriveEncoder[SimpleHeader]

  given Decoder[SimpleHeader] = deriveDecoder[SimpleHeader]

  given circeEncoder: Encoder[NestedHeader] = deriveEncoder[NestedHeader]

  given circeDecoder: Decoder[NestedHeader] = deriveDecoder[NestedHeader]

  given ClaimsEncoder[SimpleHeader] = simpleHeader => simpleHeader.asJson.noSpaces

  given failWithBoomDecoder: ClaimsDecoder[SimpleHeader] = _ => throw new RuntimeException("Boom")

  given claimsEncoder: ClaimsEncoder[NestedHeader] = nestedHeader => nestedHeader.asJson.noSpaces

  given claimsDecoder: ClaimsDecoder[NestedHeader] = nestedHeaderJson =>
    parse(nestedHeaderJson).left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[NestedHeader].left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )
}

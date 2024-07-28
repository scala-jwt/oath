package io.oath.test

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.oath.*
import io.oath.json.*
import io.oath.test.NestedHeader.SimpleHeader

final case class NestedHeader(name: String, mapping: Map[String, SimpleHeader])

object NestedHeader {
  final case class SimpleHeader(name: String, data: List[String])

  given simpleHeaderCirceEncoder: Encoder[SimpleHeader] = deriveEncoder[SimpleHeader]

  given simpleHeaderCirceDecoder: Decoder[SimpleHeader] = deriveDecoder[SimpleHeader]

  given nestedHeaderCirceEncoder: Encoder[NestedHeader] = deriveEncoder[NestedHeader]

  given nestedHeaderCirceDecoder: Decoder[NestedHeader] = deriveDecoder[NestedHeader]

  given simpleHeaderEncoder: ClaimsEncoder[SimpleHeader] = simpleHeader => simpleHeader.asJson.noSpaces

  given simpleHeaderDecoder: ClaimsDecoder[SimpleHeader] = _ => throw new RuntimeException("Boom")

  given nestedHeaderEncoder: ClaimsEncoder[NestedHeader] = nestedHeader => nestedHeader.asJson.noSpaces

  given nestedHeaderDecoder: ClaimsDecoder[NestedHeader] = nestedHeaderJson =>
    parse(nestedHeaderJson).left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[NestedHeader].left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )
}

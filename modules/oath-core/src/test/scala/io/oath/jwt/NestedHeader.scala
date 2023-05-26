package io.oath.jwt

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._
import io.oath.jwt.NestedHeader.SimpleHeader
import io.oath.jwt.model.JwtVerifyError

final case class NestedHeader(name: String, mapping: Map[String, SimpleHeader])

object NestedHeader {
  final case class SimpleHeader(name: String, data: List[String])

  implicit val simpleHeaderCirceEncoder: Encoder[SimpleHeader] = deriveEncoder[SimpleHeader]
  implicit val simpleHeaderCirceDecoder: Decoder[SimpleHeader] = deriveDecoder[SimpleHeader]

  implicit val nestedHeaderCirceEncoder: Encoder[NestedHeader] = deriveEncoder[NestedHeader]
  implicit val nestedHeaderCirceDecoder: Decoder[NestedHeader] = deriveDecoder[NestedHeader]

  implicit val simpleHeaderEncoder: ClaimsEncoder[SimpleHeader] = simpleHeader => simpleHeader.asJson.noSpaces
  implicit val simpleHeaderDecoder: ClaimsDecoder[SimpleHeader] = _ => throw new RuntimeException("Boom")

  implicit val nestedHeaderEncoder: ClaimsEncoder[NestedHeader] = nestedHeader => nestedHeader.asJson.noSpaces
  implicit val nestedHeaderDecoder: ClaimsDecoder[NestedHeader] = nestedHeaderJson =>
    parse(nestedHeaderJson).left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[NestedHeader].left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )

}

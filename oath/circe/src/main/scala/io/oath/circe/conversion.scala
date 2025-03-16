package io.oath.circe

import io.circe._
import io.circe.syntax.EncoderOps
import io.oath.JwtVerifyError
import io.oath.json._

object conversion {
  given [P](using codec: Codec[P]): ClaimsCodec[P] = new ClaimsCodec[P] {
    override def decode(token: String): Either[JwtVerifyError.DecodingError, P] =
      decoderConverter[P].decode(token)

    override def encode(data: P): String =
      encoderConverter[P].encode(data)
  }

  given encoderConverter[P](using encoder: Encoder[P]): ClaimsEncoder[P] = data => data.asJson(encoder).noSpaces

  given decoderConverter[P](using decoder: Decoder[P]): ClaimsDecoder[P] = json =>
    parser
      .parse(json)
      .left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[P](decoder).left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )
}

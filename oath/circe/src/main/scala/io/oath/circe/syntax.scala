package io.oath.circe

import io.circe.*
import io.circe.syntax.EncoderOps
import io.oath.JwtVerifyError
import io.oath.json.{ClaimsCodec, ClaimsDecoder, ClaimsEncoder}

object syntax {
  extension [P](encoder: Encoder[P]) def convert: ClaimsEncoder[P] = data => data.asJson(encoder).noSpaces

  extension [P](decoder: Decoder[P])
    def convert: ClaimsDecoder[P] =
      json =>
        parser
          .parse(json)
          .left
          .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
          .flatMap(
            _.as[P](decoder).left.map(decodingFailure =>
              JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
            )
          )

  extension [P](codec: Codec[P])
    def convertCodec: ClaimsCodec[P] = new ClaimsCodec[P] {
      override def decode(token: String): Either[JwtVerifyError.DecodingError, P] =
        codec.asInstanceOf[Decoder[P]].convert.decode(token)

      override def encode(data: P): String =
        codec.asInstanceOf[Encoder[P]].convert.encode(data)
    }
}

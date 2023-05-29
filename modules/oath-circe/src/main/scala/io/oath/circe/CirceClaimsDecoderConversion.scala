package io.oath.circe

import io.circe.{Decoder, parser}
import io.oath.json.ClaimsDecoder
import io.oath.model.JwtVerifyError

trait CirceClaimsDecoderConversion {

  implicit def decoderConversion[A](implicit decoder: Decoder[A]): ClaimsDecoder[A] = json =>
    parser
      .parse(json)
      .left
      .map(parsingFailure => JwtVerifyError.DecodingError(parsingFailure.message, parsingFailure.underlying))
      .flatMap(
        _.as[A].left.map(decodingFailure =>
          JwtVerifyError.DecodingError(decodingFailure.getMessage(), decodingFailure.getCause)
        )
      )

}

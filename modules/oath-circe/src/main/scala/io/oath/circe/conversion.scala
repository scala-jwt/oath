package io.oath.circe

import io.circe.*
import io.oath.circe.syntax.*
import io.oath.json.*

object conversion:
  given [P](using encoder: Encoder[P]): ClaimsEncoder[P] = encoder.convert

  given [P](using decoder: Decoder[P]): ClaimsDecoder[P] = decoder.convert

  given [P](using codec: Codec[P]): ClaimsCodec[P] = codec.convertCodec

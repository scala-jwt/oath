package io.oath.json

trait ClaimsCodec[P] extends ClaimsEncoder[P], ClaimsDecoder[P]

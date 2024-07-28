package io.oath.jsoniter_scala

import com.github.plokhotnyuk.jsoniter_scala.core.*
import io.oath.json.*
import io.oath.jsoniter_scala.syntax.*

object conversion:
  given [P](using codec: JsonValueCodec[P]): ClaimsCodec[P] = codec.convert

package io.oath.jwt.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core._
import io.oath.jwt.ClaimsEncoder

trait JsoniterClaimsEncoderConversion {

  implicit def encoderConversion[A](implicit codec: JsonValueCodec[A]): ClaimsEncoder[A] = data => writeToString(data)
}

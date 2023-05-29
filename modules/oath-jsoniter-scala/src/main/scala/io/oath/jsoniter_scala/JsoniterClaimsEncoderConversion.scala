package io.oath.jsoniter_scala

import com.github.plokhotnyuk.jsoniter_scala.core._
import io.oath.json.ClaimsEncoder

trait JsoniterClaimsEncoderConversion {

  implicit def encoderConversion[A](implicit codec: JsonValueCodec[A]): ClaimsEncoder[A] = data => writeToString(data)
}

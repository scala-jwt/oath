package io.oath.jsoniter_scala

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import io.oath.json.ClaimsCodec
import io.oath.jsoniter_scala.syntax.*

final case class Bar(name: String, age: Int)

object Bar:
  implicit val codecBar: ClaimsCodec[Bar] = JsonCodecMaker.make.convert

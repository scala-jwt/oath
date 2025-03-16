package io.oath.jsoniter_scala

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

final case class Bar(name: String, age: Int)

object Bar {
  given JsonValueCodec[Bar] = JsonCodecMaker.make[Bar]
}

package io.oath.jwt.utils

import com.fasterxml.jackson.databind.ObjectMapper

trait CodecUtils {

  val mapper = new ObjectMapper

  def unsafeParseJsonToJavaMap(json: String): java.util.Map[String, Object] =
    mapper.readValue(json, classOf[java.util.HashMap[String, Object]])

}

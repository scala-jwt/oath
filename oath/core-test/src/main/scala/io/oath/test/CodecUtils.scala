package io.oath.test

import com.fasterxml.jackson.databind.ObjectMapper

trait CodecUtils {
  private val mapper = new ObjectMapper()

  def unsafeParseJsonToJavaMap(json: String): java.util.Map[String, Object] =
    mapper.readValue(json, classOf[java.util.HashMap[String, Object]])
}

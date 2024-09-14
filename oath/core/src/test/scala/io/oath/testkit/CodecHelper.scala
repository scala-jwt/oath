package io.oath.testkit

import com.fasterxml.jackson.databind.ObjectMapper

object CodecHelper {
  private val mapper = new ObjectMapper()

  def unsafeParseJsonToJavaMap(json: String): java.util.Map[String, Object] =
    mapper.readValue(json, classOf[java.util.HashMap[String, Object]])
}

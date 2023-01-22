package io.oath.csrf

import java.util.Base64

object CsrfUtils {

  def constantTimeEquality(str1: String, str2: String): Boolean =
    if (str1.length != str2.length) false
    else {
      var equal = 0
      for (i <- Array.range(0, str1.length))
        equal |= str1(i) ^ str2(i)
      equal == 0
    }

  def toBase64(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.encodeToString(bytes)

}

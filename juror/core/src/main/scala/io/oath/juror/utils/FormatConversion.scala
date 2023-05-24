package io.oath.juror.utils

private[juror] object FormatConversion {

  def convertUpperCamelToLowerHyphen(str: String): String =
    str.split("(?=\\p{Lu})").map(_.trim.toLowerCase).mkString("-")
}

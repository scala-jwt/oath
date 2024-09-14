package io.oath.utils

private[oath] object Formatter {

  // TODO: not tested properly
  inline def convertUpperCamelToLowerHyphen(str: String): String =
    str.split("(?=\\p{Lu})").map(_.trim.toLowerCase).filter(_.nonEmpty).mkString("-").trim
}

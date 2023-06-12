package io.oath

import io.oath.utils.FormatConversion

trait OathEnumEntry {

  private[this] lazy val configEntryName: String = FormatConversion
    .convertUpperCamelToLowerHyphen(toString)

  // Override this for custom config names
  val configName: String = configEntryName
}

package io.oath.macros

object OathEnum {
  inline def apply[A]: Set[A] = OathEnumMacro.enumValues[A].toSet
}

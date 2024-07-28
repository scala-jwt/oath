package io.oath

object OathEnum:
  inline def apply[A]: Set[A] = OathEnumMacro.enumValues[A].toSet

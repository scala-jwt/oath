package io.oath.macros

trait EnumExtension[E] {
  def values: Set[E]
}

object EnumExtension {

  inline def apply[E](using )
}
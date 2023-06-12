package io.oath

object OathEnum {

  def apply[A]: Set[A] = macro OathEnumMacro.findEnumMembersImpl[A]
}

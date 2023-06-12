package io.oath

import io.oath.OathEnumMacro.findEnumMembersImpl

trait OathEnum[T <: OathEnumEntry] {

  protected def findTokenEnumMembers: Set[T] = macro findEnumMembersImpl[T]

  val tokenValues: Set[T]
}

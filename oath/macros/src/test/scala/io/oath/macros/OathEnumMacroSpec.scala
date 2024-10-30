package io.oath.macros

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class OathEnumMacroSpec extends AnyWordSpec with should.Matchers {

  enum Foo {
    case Foo1, Foo2, Foo3, Foo4
  }

  "OathEnumMacros" should {
    "discover all children directories (enum values) in a Sum type" in {
      val fooChildren = OathEnumMacro.enumValues[Foo]
      fooChildren should contain theSameElementsAs Set(Foo.Foo1, Foo.Foo2, Foo.Foo3, Foo.Foo4)
    }
  }
}

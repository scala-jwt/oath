package io.oath

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class OathEnumMacroSpec extends AnyWordSpec with should.Matchers {

  sealed trait Foo

  object Foo {
    case object Foo1 extends Foo
    case object Foo2 extends Foo
    case object Foo3 extends Foo
    case object Foo4 extends Foo

    val children = OathEnum[Foo]
  }

  sealed class Bar

  object Bar {
    case object Bar1 extends Bar
    case object Bar2 extends Bar
    case object Bar3 extends Bar
    case object Bar4 extends Bar

    val children = OathEnum[Bar]
  }

  sealed abstract class FooBar

  object FooBar {
    case object FooBar1 extends FooBar
    case object FooBar2 extends FooBar
    case object FooBar3 extends FooBar
    case object FooBar4 extends FooBar

    val children = OathEnum[FooBar]
  }

  "OathEnumMacros" should {

    "discover all children directories (case objects) with sealed trait" in {

      Foo.children should contain theSameElementsAs Set(Foo.Foo1, Foo.Foo2, Foo.Foo3, Foo.Foo4)
    }

    "discover all children directories (case objects) with sealed class" in {

      Bar.children should contain theSameElementsAs Set(Bar.Bar1, Bar.Bar2, Bar.Bar3, Bar.Bar4)
    }

    "discover all children directories (case objects) with sealed abstract class" in {

      FooBar.children should contain theSameElementsAs Set(
        FooBar.FooBar1,
        FooBar.FooBar2,
        FooBar.FooBar3,
        FooBar.FooBar4,
      )
    }
  }
}

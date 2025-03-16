package io.oath.macros

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class OathEnumSpec extends AnyWordSpec with should.Matchers {

  enum Foo derives OathEnum {
    case Foo1, FooBar, fooBar
  }

  sealed trait Bar derives OathEnum

  object Bar {
    case object Bar1 extends Bar
    case object Bar2 extends Bar
    case object Bar3 extends Bar
    case object Bar4 extends Bar
  }

  sealed abstract class FooBar derives OathEnum

  object FooBar {
    case object FooBar1 extends FooBar
    case object FooBar2 extends FooBar
    case object fooBar3 extends FooBar
  }

//  This should fail to compile because sealed class is not supported
//  sealed class FooBar derives OathEnum
//
//  object FooBar {
//    case object Bar1 extends FooBar
//    case object Bar4 extends FooBar
//  }

//  This should fail to compile because enum contains not product type
//  enum FooIllegal derives OathEnum {
//    case FooIllegal1(value: String) extends FooIllegal
//    case FooIllegal2 extends FooIllegal
//  }

//  This should fail to compile because sealed trait contains not product type
//  sealed trait BarIllegal derives OathEnum
//  object BarIllegal {
//    case class BarIllegal1(value: String) extends BarIllegal
//    case object BarIllegal2 extends BarIllegal
//    case object BarIllegal3 extends BarIllegal
//  }

  "OathEnum" when {
    ".value" should {
      "discover all children directories for Enum" in {
        val oathEnum = summon[OathEnum[Foo]]
        oathEnum.values should contain theSameElementsAs
          Map(Foo.Foo1 -> "foo1", Foo.FooBar -> "foo-bar", Foo.fooBar -> "foo-bar")
      }

      "discover all children directories for sealed trait" in {
        val children = summon[OathEnum[Bar]]
        children.values should contain theSameElementsAs Map(
          Bar.Bar1 -> "bar1",
          Bar.Bar2 -> "bar2",
          Bar.Bar3 -> "bar3",
          Bar.Bar4 -> "bar4",
        )
      }

      "discover all children directories for sealed abstract class" in {
        val children = summon[OathEnum[FooBar]]
        children.values should contain theSameElementsAs Map(
          FooBar.FooBar1 -> "foo-bar1",
          FooBar.FooBar2 -> "foo-bar2",
          FooBar.fooBar3 -> "foo-bar3",
        )
      }

//      "fail to compile children directories when sealed trait enum contains not enum type" in {
//        val children = EnumValues[FooIllegal]
//      }
//
//      "fail to compile children directories when sealed trait contains not enum type" in {
//        val children = EnumValues[BarIllegal]
//      }
//
//      "fail to compile for sealed abstract class" in {
//        val children = EnumValues[FooBarIllegal]
//      }
    }
  }
}

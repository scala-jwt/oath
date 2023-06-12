package io.oath.utils

import io.oath.testkit.AnyWordSpecBase
import io.oath.{OathEnum, OathEnumEntry}

class FormatConversionSpec extends AnyWordSpecBase {

  "FormatConversion" should {

    "convert upper camel case to lower hyphen" in {
      val res1 = FormatConversion.convertUpperCamelToLowerHyphen("HelloWorld")
      val res2 = FormatConversion.convertUpperCamelToLowerHyphen(" Hello World ")

      val expected = "hello-world"

      res1 shouldBe expected
      res2 shouldBe expected
    }

    "convert scala enum string values to lower hyphen" in {
      sealed trait SomeEnum extends OathEnumEntry

      object SomeEnum extends OathEnum[SomeEnum] {
        case object firstEnum extends SomeEnum
        case object SecondEnum extends SomeEnum
        case object Third extends SomeEnum
        case object ForthEnumValue extends SomeEnum

        override val tokenValues: Set[SomeEnum] = findTokenEnumMembers
      }

      val expected = Seq("first-enum", "second-enum", "third", "forth-enum-value")

      SomeEnum.tokenValues
        .map(_.configName)
        .map(FormatConversion.convertUpperCamelToLowerHyphen) should contain theSameElementsAs expected
    }
  }
}

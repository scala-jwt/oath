package io.oath.juror.utils

import enumeratum.EnumEntry
import enumeratum.Enum
import io.oath.jwt.testkit.AnyWordSpecBase

class FormatConversionSpec extends AnyWordSpecBase {

  "FormatConversion" should {

    "convert upper camel case to lower hyphen" in {
      val res1     = FormatConversion.convertUpperCamelToLowerHyphen("HelloWorld")
      val res2     = FormatConversion.convertUpperCamelToLowerHyphen(" Hello World ")

      val expected = "hello-world"

      res1 shouldBe expected
      res2 shouldBe expected
    }

    "convert scala enum string values to lower hyphen" in {
      sealed trait SomeEnum extends EnumEntry

      object SomeEnum extends Enum[SomeEnum] {
        override def values: IndexedSeq[SomeEnum] = findValues

        case object firstEnum extends SomeEnum
        case object SecondEnum extends SomeEnum
        case object Third extends SomeEnum
        case object ForthEnumValue extends SomeEnum
      }

      val expected = Seq("first-enum", "second-enum", "third", "forth-enum-value")

      SomeEnum.values
        .map(_.entryName)
        .map(FormatConversion.convertUpperCamelToLowerHyphen) should contain theSameElementsAs expected
    }
  }
}

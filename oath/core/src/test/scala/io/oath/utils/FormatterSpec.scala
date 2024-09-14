package io.oath.utils

import io.oath.testkit.*

class FormatterSpec extends AnyWordSpecBase {

  "Formatter" should {
    "convert upper camel case to lower hyphen" in {
      val res1 = Formatter.convertUpperCamelToLowerHyphen("HelloWorld")
      val res2 = Formatter.convertUpperCamelToLowerHyphen(" Hello World ")

      val expected = "hello-world"

      res1 shouldBe expected
      res2 shouldBe expected
    }

    "convert scala enum string values to lower hyphen" in {
      enum SomeEnum:
        case firstEnum, SecondEnum, Third, ForthEnumValue

      val expected = Seq("first-enum", "second-enum", "third", "forth-enum-value")

      SomeEnum.values.toSeq
        .map(_.toString)
        .map(Formatter.convertUpperCamelToLowerHyphen) should contain theSameElementsAs expected
    }
  }
}

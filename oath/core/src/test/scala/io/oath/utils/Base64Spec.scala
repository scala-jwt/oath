package io.oath.utils

import io.oath.testkit.WordSpecBase

class Base64Spec extends WordSpecBase {

  "Base64" when {
    ".decodeToken" should {}
    "decode token" in {
      val str = "dGVzdA=="
      Base64.decodeToken(str) shouldBe Right("test")
    }
  }
}

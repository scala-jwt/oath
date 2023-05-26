package io.oath.juror

import io.oath.jwt.testkit.AnyWordSpecBase

class TokenEnumSpec extends AnyWordSpecBase {

  "TokenEnum" should {

    "convert upper camel case to lower hyphen case strings" in {
      TokenSample.mapping shouldBe
        Map(
          TokenSample.AccessToken -> "access-token",
          TokenSample.refreshToken -> "refresh-token",
        )
    }
  }
}

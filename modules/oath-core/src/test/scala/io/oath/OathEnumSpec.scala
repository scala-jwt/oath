package io.oath

import io.oath.testkit.AnyWordSpecBase

class OathEnumSpec extends AnyWordSpecBase {

  "TokenEnum" should {

    "convert upper camel case to lower hyphen case strings" in {
      TokenSample.tokenValues.map(token => token -> token.configName).toMap shouldBe
        Map(
          TokenSample.AccessToken -> "access-token",
          TokenSample.refreshToken -> "refresh-token",
        )
    }
  }
}

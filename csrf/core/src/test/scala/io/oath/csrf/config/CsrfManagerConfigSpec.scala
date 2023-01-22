package io.oath.csrf.config

import com.typesafe.config.ConfigFactory
import io.oath.csrf.testkit.AnyWordSpecBase

class CsrfManagerConfigSpec extends AnyWordSpecBase {

  val TokenConfigLocation       = "token"
  val InvalidCsrfConfigLocation = "invalid-csrf"

  "CsrfLoader" should {

    "load csrf secret-key config value from reference configuration file default" in {
      val config = CsrfManagerConfig.loadOrThrow(ConfigFactory.load())

      config.secret.value shouldBe "secret"
    }

    "load csrf secret-key config value from reference configuration file using location" in {
      val config = CsrfManagerConfig.loadOrThrow(TokenConfigLocation)

      config.secret.value shouldBe "token-secret"
    }

    "fail to load csrf secret-key config value from reference configuration when secret-key is empty" in {
      the[java.lang.IllegalArgumentException] thrownBy CsrfManagerConfig.loadOrThrow(InvalidCsrfConfigLocation)
    }
  }
}

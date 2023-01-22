package io.oath.csrf

import io.oath.csrf.config.CsrfManagerConfig
import io.oath.csrf.model.CsrfToken
import io.oath.csrf.testkit.{AnyWordSpecBase, PropertyBasedTesting}

class CsrfManagerSpec extends AnyWordSpecBase with PropertyBasedTesting {

  "CsrfManager" when {

    "issueCSRF" should {
      "generate a valid token when secret is provided" in forAll { csrfConfig: CsrfManagerConfig =>
        val csrfManager  = new CsrfManager(csrfConfig)
        val csrfToken    = csrfManager.issueCSRF()
        val expectedSize = 2

        csrfToken should not be empty
        csrfToken.value.token.value.split("-", 2).length shouldBe expectedSize
        csrfToken.value.token.value.split("-").head.toLongOption.nonEmpty shouldBe true
      }
    }

    "verifyCSRF" should {
      "verify a valid token with the same secret" in forAll { csrfConfig: CsrfManagerConfig =>
        val csrfManager = new CsrfManager(csrfConfig)
        val csrfToken   = csrfManager.issueCSRF()

        csrfToken.exists(csrfManager.verifyCSRF) shouldBe true
      }

      "failed to verify when token provided has invalid structure" in {
        (csrfConfig: CsrfManagerConfig, csrfToken: CsrfToken) =>
          val csrfManager = new CsrfManager(csrfConfig)

          csrfManager.verifyCSRF(csrfToken) shouldBe false
      }

      "failed to verify when token provided has valid structure but wrong key" in {
        (csrfConfig1: CsrfManagerConfig, csrfConfig2: CsrfManagerConfig) =>
          whenever(csrfConfig1.secret != csrfConfig2.secret) {
            val csrfManager1 = new CsrfManager(csrfConfig1)
            val csrfManager2 = new CsrfManager(csrfConfig2)

            csrfManager1.verifyCSRF(csrfManager2.issueCSRF().value) shouldBe false
          }
      }
    }
  }
}

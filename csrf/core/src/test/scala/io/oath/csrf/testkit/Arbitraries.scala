package io.oath.csrf.testkit

import eu.timepit.refined.types.string.NonEmptyString
import io.oath.csrf.config.CsrfManagerConfig
import io.oath.csrf.model.{CsrfParts, CsrfToken}
import org.scalacheck.{Arbitrary, Gen}

trait Arbitraries {

  val genNonEmptyString =
    Gen.nonEmptyListOf[Char](Gen.alphaNumChar).map(_.mkString).map(NonEmptyString.unsafeFrom)

  implicit val arbCsrfConfig: Arbitrary[CsrfManagerConfig] = Arbitrary(genNonEmptyString.map(CsrfManagerConfig(_)))

  implicit val arbCsrfToken: Arbitrary[CsrfToken] = Arbitrary(genNonEmptyString.map(CsrfToken))

  implicit val arbCsrfParts: Arbitrary[CsrfParts] = Arbitrary {
    for {
      message <- genNonEmptyString
      signed  <- genNonEmptyString
    } yield CsrfParts(message, signed)
  }
}

package io.oath.test

import org.scalactic.anyvals.PosInt
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait PropertyBasedTesting extends ScalaCheckPropertyChecks, Arbitraries {
  val minSuccessful = PosInt(25)

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful)
}

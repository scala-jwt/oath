package io.oath.csrf.testkit

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}

class AnyWordSpecBase extends AnyWordSpec with should.Matchers with OptionValues with EitherValues

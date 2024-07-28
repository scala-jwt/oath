package io.oath.test

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}

abstract class AnyWordSpecBase extends AnyWordSpec, should.Matchers, OptionValues, EitherValues

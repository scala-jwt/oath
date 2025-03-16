package io.oath.testkit

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}

abstract class WordSpecBase extends AnyWordSpec, should.Matchers, OptionValues, EitherValues

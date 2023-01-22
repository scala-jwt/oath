package io.oath.csrf.model

import eu.timepit.refined.types.string.NonEmptyString

final case class CsrfParts(message: NonEmptyString, signed: NonEmptyString)

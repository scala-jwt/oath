package io.oath.csrf.model

import eu.timepit.refined.types.string.NonEmptyString

final case class CsrfToken(token: NonEmptyString)

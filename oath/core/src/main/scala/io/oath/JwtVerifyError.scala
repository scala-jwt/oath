package io.oath

import cats.syntax.all.*

sealed abstract class JwtVerifyError(error: String, cause: Option[Throwable] = None)
    extends Exception(error, cause.orNull)

object JwtVerifyError {
  final case class VerificationError(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying)

  final case class DecodingError(message: String, underlying: Throwable)
      extends JwtVerifyError(message, underlying.some)
}

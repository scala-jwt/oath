package io.oath

import cats.syntax.all.*

sealed abstract class JwtVerifyError(error: String, cause: Option[Throwable] = None)
    extends Exception(error, cause.orNull)

object JwtVerifyError {
  case class IllegalArgument(message: String, underlying: Throwable) extends JwtVerifyError(message, underlying.some)

  case class AlgorithmMismatch(message: String, underlying: Throwable) extends JwtVerifyError(message, underlying.some)

  case class DecodingError(message: String, underlying: Throwable) extends JwtVerifyError(message, underlying.some)

  case class VerificationError(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying)

  case class SignatureVerificationError(message: String, underlying: Throwable)
      extends JwtVerifyError(message, underlying.some)

  case class DecryptionError(message: String) extends JwtVerifyError(message)

  case class TokenExpired(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying)

  case class UnexpectedError(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying)
}

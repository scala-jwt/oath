package io.oath

sealed abstract class JwtVerifyError(error: String, cause: Throwable = null) extends Exception(error, cause)

object JwtVerifyError:
  case class IllegalArgument(message: String, underlying: Throwable) extends JwtVerifyError(message, underlying)
  case class AlgorithmMismatch(message: String, underlying: Throwable) extends JwtVerifyError(message, underlying)
  case class DecodingError(message: String, underlying: Throwable) extends JwtVerifyError(message, underlying)
  case class VerificationError(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying.orNull)
  case class SignatureVerificationError(message: String, underlying: Throwable)
      extends JwtVerifyError(message, underlying)
  case class DecryptionError(message: String) extends JwtVerifyError(message)
  case class TokenExpired(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying.orNull)
  case class UnexpectedError(message: String, underlying: Option[Throwable] = None)
      extends JwtVerifyError(message, underlying.orNull)

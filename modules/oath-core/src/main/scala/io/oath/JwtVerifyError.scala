package io.oath

sealed abstract class JwtVerifyError(val error: String) extends Exception(error)

object JwtVerifyError:
  case class IllegalArgument(override val error: String) extends JwtVerifyError(error)
  case class AlgorithmMismatch(override val error: String) extends JwtVerifyError(error)
  case class DecodingError(override val error: String, underlying: Throwable) extends JwtVerifyError(error)
  case class VerificationError(override val error: String) extends JwtVerifyError(error)
  case class SignatureVerificationError(override val error: String) extends JwtVerifyError(error)
  case class DecryptionError(override val error: String) extends JwtVerifyError(error)
  case class TokenExpired(override val error: String) extends JwtVerifyError(error)
  case class UnexpectedError(override val error: String) extends JwtVerifyError(error)

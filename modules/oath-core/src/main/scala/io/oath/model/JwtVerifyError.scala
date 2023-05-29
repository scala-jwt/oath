package io.oath.model

sealed abstract class JwtVerifyError(val error: String) extends Exception(error)

object JwtVerifyError {

  final case class IllegalArgument(override val error: String) extends JwtVerifyError(error)

  final case class AlgorithmMismatch(override val error: String) extends JwtVerifyError(error)

  final case class DecodingError(override val error: String, underlying: Throwable) extends JwtVerifyError(error)

  final case class VerificationError(override val error: String) extends JwtVerifyError(error)

  final case class SignatureVerificationError(override val error: String) extends JwtVerifyError(error)

  final case class DecryptionError(override val error: String) extends JwtVerifyError(error)

  final case class TokenExpired(override val error: String) extends JwtVerifyError(error)

  final case class UnexpectedError(override val error: String) extends JwtVerifyError(error)
}

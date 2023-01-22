package io.oath.jwt.model

sealed trait JwtVerifyError {
  def error: String
}

object JwtVerifyError {

  final case class IllegalArgument(error: String) extends JwtVerifyError

  final case class AlgorithmMismatch(error: String) extends JwtVerifyError

  final case class DecodingError(error: String, underlying: Throwable) extends JwtVerifyError

  final case class DecodingErrors(headerDecodingError: Option[DecodingError],
                                  payloadDecodingError: Option[DecodingError]
  ) extends JwtVerifyError {
    private val headerMessage =
      headerDecodingError.map(decodingError => s"\nheader decoding error: ${decodingError.error}")
    private val payloadMessage =
      payloadDecodingError.map(decodingError => s"\npayload decoding error: ${decodingError.error}")
    val error =
      s"JWT Failed to decode both parts: ${headerMessage.getOrElse("")} ${payloadMessage.getOrElse("")}"
  }

  final case class VerificationError(error: String) extends JwtVerifyError

  final case class SignatureVerificationError(error: String) extends JwtVerifyError

  final case class DecryptionError(error: String) extends JwtVerifyError

  final case class TokenExpired(error: String) extends JwtVerifyError

  final case class UnexpectedError(error: String) extends JwtVerifyError
}

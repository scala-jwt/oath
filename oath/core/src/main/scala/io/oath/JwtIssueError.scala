package io.oath

import cats.syntax.all.*

sealed abstract class JwtIssueError(error: String, cause: Throwable) extends Exception(error, cause)

object JwtIssueError {
  final case class SignError(message: String)(underlying: Throwable) extends JwtIssueError(message, underlying)

  final case class EncodeError(message: String)(underlying: Throwable) extends JwtIssueError(message, underlying)
}

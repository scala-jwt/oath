package io.oath

import cats.syntax.all.*

sealed abstract class JwtIssueError(error: String, cause: Option[Throwable] = None)
    extends Exception(error, cause.orNull)

object JwtIssueError {
  final case class SignError(message: String, underlying: Throwable) extends JwtIssueError(message, underlying.some)

  final case class EncodeError(message: String, underlying: Throwable) extends JwtIssueError(message, underlying.some)
}

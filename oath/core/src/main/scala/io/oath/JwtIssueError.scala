package io.oath

import cats.syntax.all.*

sealed abstract class JwtIssueError(error: String, cause: Option[Throwable] = None)
    extends Exception(error, cause.orNull)

object JwtIssueError {
  case class IllegalArgument(message: String, underlying: Throwable) extends JwtIssueError(message, underlying.some)

  case class JwtCreationIssueError(message: String, underlying: Throwable)
      extends JwtIssueError(message, underlying.some)

  case class EncryptionError(message: String) extends JwtIssueError(message)

  case class EncodeError(message: String) extends JwtIssueError(message)

  case class UnexpectedIssueError(message: String, underlying: Option[Throwable] = None)
      extends JwtIssueError(message, underlying)
}

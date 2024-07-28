package io.oath

sealed abstract class JwtIssueError(error: String, cause: Throwable = null) extends Exception(error, cause)

object JwtIssueError:
  case class IllegalArgument(message: String, underlying: Throwable) extends JwtIssueError(message, underlying)
  case class JwtCreationIssueError(message: String, underlying: Throwable) extends JwtIssueError(message, underlying)
  case class EncryptionError(message: String) extends JwtIssueError(message)
  case class EncodeError(message: String) extends JwtIssueError(message)
  case class UnexpectedIssueError(message: String, underlying: Option[Throwable] = None)
      extends JwtIssueError(message, underlying.orNull)

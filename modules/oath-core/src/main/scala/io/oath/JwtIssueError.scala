package io.oath

sealed abstract class JwtIssueError(val error: String) extends Exception(error)

object JwtIssueError:
  case class IllegalArgument(override val error: String) extends JwtIssueError(error)
  case class JwtCreationIssueError(override val error: String) extends JwtIssueError(error)
  case class EncryptionError(override val error: String) extends JwtIssueError(error)
  case class EncodeError(override val error: String) extends JwtIssueError(error)
  case class UnexpectedIssueError(override val error: String) extends JwtIssueError(error)

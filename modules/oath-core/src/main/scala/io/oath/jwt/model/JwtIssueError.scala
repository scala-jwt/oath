package io.oath.jwt.model

sealed abstract class JwtIssueError(val error: String) extends Exception(error)

object JwtIssueError {

  final case class IllegalArgument(override val error: String) extends JwtIssueError(error)

  final case class JwtCreationIssueError(override val error: String) extends JwtIssueError(error)

  final case class EncryptionError(override val error: String) extends JwtIssueError(error)

  final case class EncodeError(override val error: String) extends JwtIssueError(error)

  final case class UnexpectedIssueError(override val error: String) extends JwtIssueError(error)
}

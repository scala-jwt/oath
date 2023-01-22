package io.oath.jwt.model

sealed trait JwtIssueError {
  def error: String
}

object JwtIssueError {

  final case class IllegalArgument(error: String) extends JwtIssueError

  final case class JwtCreationIssueError(error: String) extends JwtIssueError

  final case class EncryptionError(error: String) extends JwtIssueError

  final case class EncodeError(error: String) extends JwtIssueError

  final case class UnexpectedIssueError(error: String) extends JwtIssueError
}

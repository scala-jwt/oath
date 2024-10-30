package io.oath

import com.auth0.jwt.interfaces.DecodedJWT
import io.oath.macros.OathEnumMacro
import io.oath.utils.Formatter

import java.time.Instant
import scala.jdk.CollectionConverters.CollectionHasAsScala

// TODO: Move to a file and test it properly
inline def getEnumValues[A](using
    m: scala.deriving.Mirror.SumOf[A]
): Set[(A, String)] =
  OathEnumMacro
    .enumValues[A]
    .map(value => value -> Formatter.convertUpperCamelToLowerHyphen(value.toString))

// TODO: Move to file
extension (decodedJWT: DecodedJWT) {
  def getOptionIssuer: Option[String]  = Option(decodedJWT.getIssuer)
  def getOptionSubject: Option[String] = Option(decodedJWT.getSubject)
  def getSeqAudience: Seq[String] =
    Option(decodedJWT.getAudience).map(_.asScala).toSeq.flatten
  def getOptionExpiresAt: Option[Instant] = Option(decodedJWT.getExpiresAt).map(_.toInstant)
  def getOptionNotBefore: Option[Instant] = Option(decodedJWT.getNotBefore).map(_.toInstant)
  def getOptionIssueAt: Option[Instant]   = Option(decodedJWT.getIssuedAt).map(_.toInstant)
  def getOptionJwtID: Option[String]      = Option(decodedJWT.getId)
}

import sbt.*

object Dependencies {

  private lazy val scalaTestV          = "3.2.19"
  private lazy val scalaTestPlusCheckV = "3.2.18.0"
  private lazy val scalacheckV         = "1.17.1"
  private lazy val javaJWTV            = "4.4.0"
  private lazy val configV             = "1.4.3"
  private lazy val bcprovV             = "1.78.1"
  private lazy val circeV              = "0.14.9"
  private lazy val jsoniterScalaV      = "2.30.7"
  private lazy val catsV               = "2.12.0"
  private lazy val tinkV               = "1.14.1"

  lazy val scalaTest               = "org.scalatest"     %% "scalatest"       % scalaTestV
  lazy val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-17" % scalaTestPlusCheckV
  lazy val scalacheck              = "org.scalacheck"    %% "scalacheck"      % scalacheckV

  lazy val circeCore    = "io.circe" %% "circe-core"    % circeV
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeV
  lazy val circeParser  = "io.circe" %% "circe-parser"  % circeV

  lazy val jsoniterScalacore = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterScalaV
  lazy val jsoniterScalamacros =
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterScalaV % "provided"

  lazy val cats           = "org.typelevel"         %% "cats-core"      % catsV
  lazy val typesafeConfig = "com.typesafe"           % "config"         % configV
  lazy val bcprov         = "org.bouncycastle"       % "bcprov-jdk18on" % bcprovV
  lazy val tink           = "com.google.crypto.tink" % "tink"           % tinkV

  lazy val javaJWT = "com.auth0" % "java-jwt" % javaJWTV
}

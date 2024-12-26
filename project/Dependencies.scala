import sbt.*

object Dependencies {

  private lazy val scalaTestV          = "3.2.19"
  private lazy val scalaTestPlusCheckV = "3.2.18.0"
  private lazy val scalacheckV         = "1.18.1"
  private lazy val javaJWTV            = "4.4.0"
  private lazy val configV             = "1.4.3"
  private lazy val bcprovV             = "1.79"
  private lazy val circeV              = "0.14.10"
  private lazy val jsoniterScalaV      = "2.31.3"
  private lazy val catsV               = "2.12.0"
  private lazy val tinkV               = "1.15.0"

  // Testing
  lazy val scalaTest               = "org.scalatest"     %% "scalatest"       % scalaTestV
  lazy val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-17" % scalaTestPlusCheckV
  lazy val scalacheck              = "org.scalacheck"    %% "scalacheck"      % scalacheckV

  // Circe
  lazy val circeCore    = "io.circe" %% "circe-core"    % circeV
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeV
  lazy val circeParser  = "io.circe" %% "circe-parser"  % circeV

  // Jsoniter-scala
  lazy val jsoniterScalacore = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterScalaV
  lazy val jsoniterScalamacros =
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterScalaV % "provided"

  // Typelevel
  lazy val catsCore = "org.typelevel" %% "cats-core" % catsV

  lazy val typesafeConfig = "com.typesafe"           % "config"         % configV
  lazy val bcprov         = "org.bouncycastle"       % "bcprov-jdk18on" % bcprovV
  lazy val tink           = "com.google.crypto.tink" % "tink"           % tinkV

  lazy val javaJWT = "com.auth0" % "java-jwt" % javaJWTV
}

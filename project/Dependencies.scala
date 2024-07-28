import sbt.*

object Dependencies {

  val scalaTestV          = "3.2.19"
  val scalaTestPlusCheckV = "3.2.17.0"
  val scalacheckV         = "1.17.1"
  val javaJWTV            = "4.4.0"
  val configV             = "1.4.3"
  val bcprovV             = "1.78.1"
  val circeV              = "0.14.7"
  val jsoniterScalaV      = "2.27.3"
  val catsV               = "2.12.0"

  val scalaTest               = "org.scalatest"     %% "scalatest"       % scalaTestV
  val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-17" % scalaTestPlusCheckV
  val scalacheck              = "org.scalacheck"    %% "scalacheck"      % scalacheckV

  val circeCore    = "io.circe" %% "circe-core"    % circeV
  val circeGeneric = "io.circe" %% "circe-generic" % circeV
  val circeParser  = "io.circe" %% "circe-parser"  % circeV

  val jsoniterScalacore = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterScalaV
  val jsoniterScalamacros =
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterScalaV % "provided"

  val cats           = "org.typelevel"   %% "cats-core"      % catsV
  val typesafeConfig = "com.typesafe"     % "config"         % configV
  val bcprov         = "org.bouncycastle" % "bcprov-jdk18on" % bcprovV

  val javaJWT = "com.auth0" % "java-jwt" % javaJWTV
}

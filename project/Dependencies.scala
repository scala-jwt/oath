import sbt.Keys._
import sbt._

object Dependencies {

  object Versions {
    val scalaTest          = "3.2.16"
    val scalaTestPlusCheck = "3.2.14.0
    val scalacheck         = "1.17.0"
    val javaJWT            = "4.4.0"
    val config             = "1.4.2"
    val bcprov             = "1.73"
    val circe              = "0.14.5"
    val jsoniterScala      = "2.23.1"
    val enumeratum         = "1.7.2"
  }

  object Testing {
    val scalaTest          = "org.scalatest"     %% "scalatest"       % Versions.scalaTest          % Test
    val scalaTestPlusCheck = "org.scalatestplus" %% "scalacheck-1-16" % Versions.scalaTestPlusCheck % Test
    val scalacheck         = "org.scalacheck"    %% "scalacheck"      % Versions.scalacheck         % Test

    val all = Seq(scalaTest, scalaTestPlusCheck, scalacheck)
  }

  object Circe {
    val core    = "io.circe" %% "circe-core"    % Versions.circe
    val generic = "io.circe" %% "circe-generic" % Versions.circe
    val parser  = "io.circe" %% "circe-parser"  % Versions.circe

    val all = Seq(core, generic, parser)
  }

  object JsoniterScala {
    val core = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % Versions.jsoniterScala
    val macros =
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Versions.jsoniterScala % "provided"

    val all = Seq(core, macros)
  }

  object Utils {
    val config     = "com.typesafe"     % "config"         % Versions.config
    val bcprov     = "org.bouncycastle" % "bcprov-jdk18on" % Versions.bcprov
    val enumeratum = "com.beachape"    %% "enumeratum"     % Versions.enumeratum

    val juror = Seq(enumeratum)
    val jwt   = Seq(config, bcprov)
  }

  object Auth0 {
    val javaJWT = "com.auth0" % "java-jwt" % Versions.javaJWT

    val all = Seq(javaJWT)
  }

  lazy val jwtCore =
    libraryDependencies ++= Testing.all ++ Auth0.all ++ Utils.jwt ++ Circe.all.map(_ % Test)

  lazy val jwtCirce =
    libraryDependencies ++= Circe.all

  lazy val jwtJsoniterScala =
    libraryDependencies ++= JsoniterScala.all

  lazy val juror =
    libraryDependencies ++= Utils.juror
}

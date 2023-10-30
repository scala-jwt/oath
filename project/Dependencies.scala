import sbt.*
import sbt.Keys.*

object Dependencies {

  object Versions {
    val scalaTest          = "3.2.17"
    val scalaTestPlusCheck = "3.2.17.0"
    val scalacheck         = "1.17.0"
    val javaJWT            = "4.4.0"
    val config             = "1.4.2"
    val bcprov             = "1.76"
    val circe              = "0.14.6"
    val jsoniterScala      = "2.24.4"
  }

  object Testing {
    val scalaTest          = "org.scalatest"     %% "scalatest"       % Versions.scalaTest          % Test
    val scalaTestPlusCheck = "org.scalatestplus" %% "scalacheck-1-17" % Versions.scalaTestPlusCheck % Test
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
    val config = "com.typesafe"     % "config"         % Versions.config
    val bcprov = "org.bouncycastle" % "bcprov-jdk18on" % Versions.bcprov

    val all = Seq(config, bcprov)
  }

  object Auth0 {
    val javaJWT = "com.auth0" % "java-jwt" % Versions.javaJWT

    val all = Seq(javaJWT)
  }

  lazy val oathMacros =
    libraryDependencies ++= Testing.all

  lazy val oathCore =
    libraryDependencies ++= Testing.all ++ Auth0.all ++ Utils.all ++ Circe.all.map(_ % Test)

  lazy val oathCirce =
    libraryDependencies ++= Circe.all

  lazy val oathJsoniterScala =
    libraryDependencies ++= JsoniterScala.all

}

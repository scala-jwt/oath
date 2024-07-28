import Dependencies.*
import org.typelevel.sbt.gha.Permissions

import scala.util.chaining.*

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.4.1"
ThisBuild / organization := "io.github.scala-jwt"
ThisBuild / organizationName := "oath"
ThisBuild / organizationHomepage := Some(url("https://github.com/scala-jwt/oath"))
ThisBuild / tlBaseVersion := "2.0"
ThisBuild / tlMimaPreviousVersions := Set.empty
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("andrewrigas", "Andreas Rigas")
)
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / startYear := Some(2022)
ThisBuild / githubWorkflowPermissions := Some(Permissions.WriteAll)
ThisBuild / githubWorkflowJavaVersions := Seq("11", "17", "21").map(JavaSpec.temurin)
ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    id     = "checklint",
    name   = "Check code style",
    scalas = List(scalaVersion.value),
    steps = List(WorkflowStep.Checkout) ++ WorkflowStep.SetupJava(
      List(githubWorkflowJavaVersions.value.last)
    ) ++ githubWorkflowGeneratedCacheSteps.value ++ List(
      WorkflowStep.Sbt(
        List("checkLint"),
        name = Some("Check Scalafmt and Scalafix rules"),
      )
    ),
  ),
  WorkflowJob(
    id     = "Codecov",
    name   = "Codecov",
    scalas = List(scalaVersion.value),
    steps = List(WorkflowStep.Checkout) ++ WorkflowStep.SetupJava(
      List(githubWorkflowJavaVersions.value.last)
    ) ++ githubWorkflowGeneratedCacheSteps.value ++ List(
      WorkflowStep.Sbt(List("coverage", "test", "coverageAggregate")),
      WorkflowStep.Use(
        UseRef.Public(
          "codecov",
          "codecov-action",
          "v3.1.1",
        )
      ),
    ),
  ),
)

ThisBuild / Test / fork := true
ThisBuild / run / fork := true
ThisBuild / Test / parallelExecution := true
ThisBuild / scalafmtOnCompile := sys.env.getOrElse("RUN_SCALAFMT_ON_COMPILE", "false").toBoolean
ThisBuild / scalafixOnCompile := sys.env.getOrElse("RUN_SCALAFIX_ON_COMPILE", "false").toBoolean
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := "4.8.15"

lazy val rootModuleName = "root"

def rootModule(rootModule: String)(subModule: String): Project =
  Project(s"$rootModule-$subModule", file(s"$rootModule${if (subModule == rootModuleName) "" else s"/$subModule"}"))
    .pipe(project =>
      if (subModule == rootModuleName) project.enablePlugins(NoPublishPlugin)
      else project
    )

lazy val root = Project("oath", file("."))
  .enablePlugins(NoPublishPlugin)
  .settings(Aliases.all)
  .aggregate(allModules *)

lazy val example = project
  .in(file("example"))
  .dependsOn(oathCore, oathCirce, oathJsoniterScala)

val createOathModule = rootModule("oath") _

lazy val oathRoot = createOathModule("root")
  .aggregate(oathModules *)

lazy val oathMacros = createOathModule("macros")
  .settings(
    libraryDependencies ++= Seq(
      scalaTest               % Test,
      scalaTestPlusScalaCheck % Test,
      scalacheck              % Test,
    )
  )

lazy val oathCore = createOathModule("core")
  .dependsOn(oathMacros)
  .settings(
    libraryDependencies ++= Seq(
      javaJWT,
      typesafeConfig,
      bcprov,
      cats,
    )
  )

lazy val oathCoreTest = createOathModule("core-test")
  .enablePlugins(NoPublishPlugin)
  .dependsOn(oathCore)
  .settings(
    libraryDependencies ++= Seq(
      scalaTest,
      scalaTestPlusScalaCheck,
      scalacheck,
      circeCore,
      circeGeneric,
      circeParser,
    )
  )

lazy val oathCirce = createOathModule("circe")
  .dependsOn(
    oathCore,
    oathCoreTest % Test,
  )
  .settings(libraryDependencies ++= Seq(circeCore, circeGeneric, circeParser))

lazy val oathJsoniterScala = createOathModule("jsoniter-scala")
  .dependsOn(
    oathCore,
    oathCoreTest % Test,
  )
  .settings(libraryDependencies ++= Seq(jsoniterScalacore, jsoniterScalamacros))

lazy val oathModules: Seq[ProjectReference] = Seq(
  oathMacros,
  oathCore,
  oathCoreTest,
  oathCirce,
  oathJsoniterScala,
)

lazy val exampleModules: Seq[ProjectReference] = Seq(example)

lazy val allModules: Seq[ProjectReference] = exampleModules ++ oathModules

import org.typelevel.sbt.gha.Permissions

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.3.3"
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
ThisBuild / Test / parallelExecution := false
ThisBuild / scalafmtOnCompile := sys.env.getOrElse("RUN_SCALAFMT_ON_COMPILE", "false").toBoolean
ThisBuild / scalafixOnCompile := sys.env.getOrElse("RUN_SCALAFIX_ON_COMPILE", "false").toBoolean
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := "4.8.15"

def rootModule(rootModule: String)(subModule: String): Project =
  Project(s"$rootModule-$subModule", file(s"$rootModule${if (subModule == "root") "" else s"/$subModule"}"))

lazy val root = Project("oath", file("."))
  .enablePlugins(NoPublishPlugin)
  .settings(Aliases.all)
  .aggregate(allModules *)

lazy val example = project
  .in(file("example"))
  .dependsOn(oathCore)

val createOathModule = rootModule("oath") _

lazy val oathRoot = createOathModule("root")
  .aggregate(oathModules *)

lazy val oathMacros = createOathModule("macros")
  .settings(Dependencies.oathMacros)

lazy val oathCore = createOathModule("core")
  .dependsOn(oathMacros)
  .settings(Dependencies.oathCore)

lazy val oathCirce = createOathModule("circe")
  .settings(Dependencies.oathCirce)
  .dependsOn(oathCore % "compile->compile;test->test")

lazy val oathJsoniterScala = createOathModule("jsoniter-scala")
  .settings(Dependencies.oathJsoniterScala)
  .dependsOn(oathCore % "compile->compile;test->test")

lazy val oathModules: Seq[ProjectReference] = Seq(
  oathMacros,
  oathCore,
  oathCirce,
  oathJsoniterScala,
)

lazy val exampleModules: Seq[ProjectReference] = Seq(example)

lazy val allModules: Seq[ProjectReference] = exampleModules ++ oathModules

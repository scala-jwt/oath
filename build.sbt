Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / version := "0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "io.github.scala-jwt"
ThisBuild / organizationName := "oath"
ThisBuild / organizationHomepage := Some(url("https://github.com/scala-jwt/oath"))
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / tlBaseVersion := "1.0"
ThisBuild / tlMimaPreviousVersions := Set.empty
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("andrewrigas", "Andreas Rigas")
)
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / startYear := Some(2022)

ThisBuild / githubWorkflowJavaVersions := Seq("11", "17").map(JavaSpec.temurin)
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

lazy val root = Projects
  .createModule("oath", ".")
  .enablePlugins(NoPublishPlugin)
  .settings(Aliases.all)
  .aggregate(modules *)

lazy val oathCore = Projects
  .createModule("oath-core", "modules/oath-core")
  .settings(Dependencies.oathCore)

lazy val oathCirce = Projects
  .createModule("oath-circe", "modules/oath-circe")
  .settings(Dependencies.oathCirce)
  .dependsOn(oathCore % "compile->compile;test->test")

lazy val oathJsoniterScala = Projects
  .createModule("oath-jsoniter-scala", "modules/oath-jsoniter-scala")
  .settings(Dependencies.oathJsoniterScala)
  .dependsOn(oathCore % "compile->compile;test->test")

lazy val modules: Seq[ProjectReference] = Seq(
  oathCore,
  oathCirce,
  oathJsoniterScala,
)

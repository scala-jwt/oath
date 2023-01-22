Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "io.github.scala-jwt"
ThisBuild / organizationName := "oath"
ThisBuild / organizationHomepage := Some(url("https://github.com/scala-jwt/oath"))
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / coverageEnabled := true
ThisBuild / tlBaseVersion := "0.0"
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("andrewrigas", "Andreas Rigas")
)
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / startYear := Some(2022)

ThisBuild / githubWorkflowJavaVersions := Seq("11", "17").map(JavaSpec.temurin)
ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    id = "checklint",
    name = "Check code style",
    scalas = List(scalaVersion.value),
    steps = List(WorkflowStep.Checkout) ++ WorkflowStep.SetupJava(
      List(githubWorkflowJavaVersions.value.last)
    ) ++ githubWorkflowGeneratedCacheSteps.value ++ List(
      WorkflowStep.Sbt(
        List("checkLint"),
        name = Some("Check Scalafmt and Scalafix rules")
      )
    )
  ),
  WorkflowJob(
    id = "Codecov",
    name = "Codecov",
    scalas = List(scalaVersion.value),
    steps = List(WorkflowStep.Checkout) ++ WorkflowStep.SetupJava(
      List(githubWorkflowJavaVersions.value.last)
    ) ++ githubWorkflowGeneratedCacheSteps.value ++ List(
      WorkflowStep.Sbt(List("coverage", "test", "coverageAggregate")),
      WorkflowStep.Use(
        UseRef.Public(
          "codecov",
          "codecov-action",
          "v3.1.1"
        )
      )
    )
  )
)

lazy val root = Projects
  .createModule("oath", ".")
  .enablePlugins(NoPublishPlugin)
  .settings(Aliases.all)
  .aggregate(modules: _*)

lazy val jwtCore = Projects
  .createModule("jwt-core", "jwt/core")
  .settings(Dependencies.jwtCore)

lazy val jwtCirce = Projects
  .createModule("jwt-circe", "jwt/circe")
  .settings(Dependencies.jwtCirce)
  .dependsOn(jwtCore % "compile->compile;test->test")

lazy val jwtJsoniterScala = Projects
  .createModule("jwt-jsoniter-scala", "jwt/jsoniter-scala")
  .settings(Dependencies.jwtJsoniterScala)
  .dependsOn(jwtCore % "compile->compile;test->test")

lazy val juror = Projects
  .createModule("juror", "juror/core")
  .settings(Dependencies.juror)
  .dependsOn(jwtCore % "compile->compile;test->test")

lazy val csrfCore = Projects
  .createModule("csrf-core", "csrf/core")
  .settings(Dependencies.csrfCore)

lazy val modules: Seq[ProjectReference] = Seq(
  juror,
  jwtCore,
  jwtCirce,
  jwtJsoniterScala,
  csrfCore
)

import org.typelevel.sbt.gha.Permissions

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.4.2"
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
ThisBuild / Test / testForkedParallel := true
ThisBuild / scalafmtOnCompile := sys.env.getOrElse("RUN_SCALAFMT_ON_COMPILE", "false").toBoolean
ThisBuild / scalafixOnCompile := sys.env.getOrElse("RUN_SCALAFIX_ON_COMPILE", "false").toBoolean
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := "4.8.15"

def rootModule(rootModule: String)(subModule: Option[String]): Project =
  Project(
    s"$rootModule${subModule.map("-" + _).getOrElse("")}",
    file(s"$rootModule${subModule.map("/" + _).getOrElse("")}"),
  )

lazy val root = Project("oath-root", file("."))
  .enablePlugins(NoPublishPlugin)
  .settings(Aliases.all)
  .aggregate(allModules *)

lazy val example = project
  .in(file("example"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(oathCore, oathCirce, oathJsoniterScala)

lazy val createOathModule = rootModule("oath") _

lazy val oathRoot = createOathModule(None)
  .enablePlugins(NoPublishPlugin)
  .aggregate(oathModules *)

lazy val oathMacros = createOathModule(Some("macros"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.scalaTest               % Test,
      Dependencies.scalaTestPlusScalaCheck % Test,
      Dependencies.scalacheck              % Test,
    )
  )

lazy val oathCore = createOathModule(Some("core"))
  .dependsOn(oathMacros)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.javaJWT,
      Dependencies.typesafeConfig,
      Dependencies.bcprov,
      Dependencies.cats,
      Dependencies.tink,
      Dependencies.scalaTest               % Test,
      Dependencies.scalaTestPlusScalaCheck % Test,
      Dependencies.scalacheck              % Test,
      Dependencies.circeCore               % Test,
      Dependencies.circeGeneric            % Test,
      Dependencies.circeParser             % Test,
    )
  )

lazy val oathCoreTest = createOathModule(Some("core-test"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(oathCore)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.scalaTest,
      Dependencies.scalaTestPlusScalaCheck,
      Dependencies.scalacheck,
      Dependencies.circeCore,
      Dependencies.circeGeneric,
      Dependencies.circeParser,
    )
  )

lazy val oathCirce = createOathModule(Some("circe"))
  .dependsOn(
    oathCore,
    oathCoreTest % Test,
  )
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.circeCore,
      Dependencies.circeGeneric,
      Dependencies.circeParser,
    )
  )

lazy val oathJsoniterScala = createOathModule(Some("jsoniter-scala"))
  .dependsOn(
    oathCore,
    oathCoreTest % Test,
  )
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.jsoniterScalacore,
      Dependencies.jsoniterScalamacros,
    )
  )

lazy val oathModules: Seq[ProjectReference] = Seq(
  oathMacros,
  oathCore,
  oathCoreTest,
  oathCirce,
  oathJsoniterScala,
)

lazy val exampleModules: Seq[ProjectReference] = Seq(example)

lazy val allModules: Seq[ProjectReference] = exampleModules ++ oathModules

import sbt.addCommandAlias

object Aliases {

  lazy val all = scalaFmt ++ scalaFix ++ scalaLint

  lazy val scalaLint = addCommandAlias("checkLint", "checkFix; checkFmt") ++
    addCommandAlias("runLint", "runFix; runFmt")

  lazy val scalaFmt = addCommandAlias("checkFmt", "scalafmtCheckAll; scalafmtSbtCheck") ++
    addCommandAlias("runFmt", "scalafmtAll; scalafmtSbt")

  lazy val scalaFix = addCommandAlias("checkFix", "scalafixAll --check OrganizeImports; scalafixAll --check") ++
    addCommandAlias("runFix", "scalafixAll OrganizeImports; scalafixAll")
}

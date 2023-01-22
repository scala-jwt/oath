import sbt.Keys._
import sbt._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixOnCompile

object Projects {

  def createModule(moduleName: String): Project = createModule(moduleName, moduleName)

  def createModule(moduleName: String, fileName: String): Project =
    Project(moduleName, base = file(fileName))
      .settings(
        Test / fork := true,
        run / fork := true,
        Test / parallelExecution := false,
        scalafmtOnCompile := sys.env.getOrElse("RUN_SCALAFMT_ON_COMPILE", "false").toBoolean,
        scalafixOnCompile := sys.env.getOrElse("RUN_SCALAFIX_ON_COMPILE", "false").toBoolean,
        semanticdbEnabled := true,
        semanticdbVersion := "4.6.0"
      )
}

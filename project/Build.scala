import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .configs(Testing.configs: _*)
      .settings(Defaults.defaultSettings ++
                Testing.settings ++
                Packaging.settings ++
                moreSettings: _*)

  lazy val moreSettings = Seq(
    unmanagedResourceDirectories in Compile <+=
      baseDirectory { _ / "resources" },
    sourceGenerators in Compile <+= Autogen.sourceGeneratorTask,
    resourceGenerators in Compile <+= I18n.resourceGeneratorTask,
    mainClass in (Compile, packageBin) :=
      Some("org.nlogo.app.App"),
    Extensions.extensionsTask
  )

}

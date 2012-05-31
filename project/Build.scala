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

  // surely there's some better way to do these - ST 5/30/12
  lazy val threed = TaskKey[Unit]("threed", "enable NetLogo 3D")
  lazy val nogen = TaskKey[Unit]("nogen", "disable bytecode generator")

  lazy val moreSettings = Seq(
    unmanagedResourceDirectories in Compile <+=
      baseDirectory { _ / "resources" },
    sourceGenerators in Compile <+= Autogen.sourceGeneratorTask,
    resourceGenerators in Compile <+= I18n.resourceGeneratorTask,
    mainClass in (Compile, packageBin) :=
      Some("org.nlogo.app.App"),
    Extensions.extensionsTask,
    threed := { System.setProperty("org.nlogo.is3d", "true") },
    nogen  := { System.setProperty("org.nlogo.noGenerator", "true") }
  )

}

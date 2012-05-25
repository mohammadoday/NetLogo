import sbt._
import Keys._

object NetLogoBuild extends Build {
  lazy val root =
    Project(
      id = "NetLogo",
      base = file("."),
      settings = Defaults.defaultSettings ++ Seq(
        sourceGenerators in Compile <+= Autogen.sourceGeneratorTask
      ))
}

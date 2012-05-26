import sbt._
import Keys._

object NetLogoBuild extends Build {
  lazy val root =
    Project(
      id = "NetLogo",
      base = file("."),
      settings = Defaults.defaultSettings ++ Seq(
        unmanagedResourceDirectories in Compile <+=
          baseDirectory { _ / "resources" },
        sourceGenerators in Compile <+= Autogen.sourceGeneratorTask,
        mainClass in (Compile, packageBin) :=
          Some("org.nlogo.app.App"),
        packageOptions <+= dependencyClasspath in Runtime map {
          classpath =>
            Package.ManifestAttributes((
              "Class-Path", classpath.files
                .map(f => "lib/" + f.getName)
                .filter(_.endsWith(".jar"))
                .mkString(" ")))},
        packageBin in Compile ~= { jar =>
          IO.copyFile(jar, file(".") / "NetLogo.jar")
          jar
        }
      )
    )
}

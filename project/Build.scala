import sbt._
import Keys._

object NetLogoBuild extends Build {

  lazy val root =
    Project(id = "NetLogo", base = file("."))
      .settings(Defaults.defaultSettings ++
                mySettings: _*)

  lazy val mySettings = Seq(
    unmanagedResourceDirectories in Compile <+=
      baseDirectory { _ / "resources" },
    sourceGenerators in Compile <+= Autogen.sourceGeneratorTask,
    resourceGenerators in Compile <+= I18n.resourceGeneratorTask,
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
      // temporary hack until we get ProGuard going to shrink the lite jar - ST 5/25/12
      IO.download(new URL("http://ccl.northwestern.edu/netlogo/5.0.1/NetLogoLite.jar"),
                  file(".") / "NetLogoLite.jar")
      jar
    },
    Extensions.extensionsTask
  )

}

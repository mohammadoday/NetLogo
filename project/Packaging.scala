import sbt._
import Keys._
import java.io.File

object Packaging {

  val settings = Seq(
    artifactName := { (_, _, _) => "NetLogo.jar" },
    packageOptions <+= dependencyClasspath in Runtime map {
      classpath =>
        Package.ManifestAttributes((
          "Class-Path", classpath.files
            .map(f => "lib/" + f.getName)
            .filter(_.endsWith(".jar"))
            .mkString(" ")))},
    packageBin in Compile <<= (packageBin in Compile, scalaInstance, streams) map {
      (jar, instance, s) =>
        IO.copyFile(jar, file(".") / "NetLogo.jar")
        IO.delete(file(".") / "NetLogoLite.jar")
        val java5 = file(".") / "dist" / "java5" / "classes.jar"
        val url = new java.net.URL("http://ccl.northwestern.edu/devel/java5-classes.jar")
        if(!java5.exists) IO.download(url, java5)
        runProGuard(instance.libraryJar.getAbsolutePath, "lite", s.log)
        jar
      }
  )

  private def runProGuard(scalaLibraryPath: String, config: String, log: Logger) {
    log.info("building " + config + " jar")
    def doIt() {
      System.setProperty("org.nlogo.scala-library", scalaLibraryPath)
      proguard.ProGuard.main(Array("@project/proguard/" + config + ".txt"))
    }
    assert(TrapExit(doIt(), log) == 0)
  }

}

import sbt._
import Keys._

object Testing {

  val FastTest = config("fast") extend(Test)
  val SlowTest = config("slow") extend(Test)
  
  val configs = Seq(Testing.FastTest, Testing.SlowTest)

  val settings =
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    Seq(
      testOptions in FastTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(fastFilter(path, _))) },
      testOptions in SlowTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(slowFilter(path, _))) }
    )

  def fastFilter(path: Classpath, name: String): Boolean = !slowFilter(path, name)
  def slowFilter(path: Classpath, name: String): Boolean = {
    val jars = path.files.map(_.asURL).toArray[java.net.URL]
    val loader = new java.net.URLClassLoader(jars, getClass.getClassLoader)
    def clazz(name: String) = Class.forName(name, false, loader)
    clazz("org.nlogo.util.SlowTest").isAssignableFrom(clazz(name))
  }

}

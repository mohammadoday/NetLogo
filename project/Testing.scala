import sbt._
import Keys._

object Testing {

  val FastTest = config("fast") extend(Test)
  val SlowTest = config("slow") extend(Test)
  
  val configs = Seq(FastTest, SlowTest)

  lazy val tr = TaskKey[Unit]("tr", "run TestReporters", test)
  lazy val tc = TaskKey[Unit]("tc", "run TestCommands", test)
  lazy val te = TaskKey[Unit]("te", "run TestExtensions", test)
  lazy val tm = TaskKey[Unit]("tm", "run TestModels", test)

  val settings =
    inConfig(FastTest)(Defaults.testTasks) ++
    inConfig(SlowTest)(Defaults.testTasks) ++
    Seq(
      testOptions in FastTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(fastFilter(path, _))) },
      testOptions in SlowTest <<= (fullClasspath in Test) map { path =>
        Seq(Tests.Filter(slowFilter(path, _))) },
      tr <<= Testing.oneTest(tr, "org.nlogo.headless.TestReporters"),
      tc <<= Testing.oneTest(tc, "org.nlogo.headless.TestCommands"),
      tm <<= Testing.oneTest(tm, "org.nlogo.headless.TestModels"),
      te <<= Testing.oneTest(te, "org.nlogo.headless.TestExtensions")
    ) ++
    Seq(tr, tc, tm, te).flatMap(Defaults.testTaskOptions)

  private def fastFilter(path: Classpath, name: String): Boolean = !slowFilter(path, name)
  private def slowFilter(path: Classpath, name: String): Boolean = {
    val jars = path.files.map(_.asURL).toArray[java.net.URL]
    val loader = new java.net.URLClassLoader(jars, getClass.getClassLoader)
    def clazz(name: String) = Class.forName(name, false, loader)
    clazz("org.nlogo.util.SlowTest").isAssignableFrom(clazz(name))
  }

  // mostly copy-and-pasted from Defaults.testOnlyTask.  This is the best I can figure out for
  // 0.11, but it appears to me that the test-only stuff has been refactored in 0.12 and 0.13 in
  // a way that might make this easier.  see e.g.
  // github.com/harrah/xsbt/commit/fe753768d93ebeaf59c9435059b583a7b2e744d3 - ST 5/31/12
  private def oneTest(key: TaskKey[_], name: String) =
    (streams in key, loadedTestFrameworks in Test, parallelExecution in key, testOptions in key, testLoader in Test, definedTests in Test) flatMap {
      case (s, frameworks, par, opts, loader, discovered) =>
        val filter = Tests.Filter(Defaults.selectedFilter(Seq(name)))
        Tests(frameworks, loader, discovered, filter +: opts, par, "not found", s.log) map { results =>
          Tests.showResults(s.log, results)
        }
    }

}

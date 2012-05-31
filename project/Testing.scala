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
      testOptions in FastTest := Seq(Tests.Filter(fastFilter)),
      testOptions in SlowTest := Seq(Tests.Filter(slowFilter))
    )

  def fastFilter(name: String): Boolean = false
  def slowFilter(name: String): Boolean = true

}

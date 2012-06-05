import sbt._
import Keys._

// check whether the code is secretly engaging in forbidden dependencies!
// and rat on it if it is!

object Depend {

  val depend = TaskKey[Unit](
    "depend", "use Classycle to ferret out forbidden dependencies")

  lazy val dependTask =
    depend <<= (fullClasspath in Test, baseDirectory, classDirectory in Compile, classDirectory in Test, streams).map{
      (cp, base, classes, testClasses, s) =>
        IO.write(base / "devel" / "depend.ddf", ddfContents)
        import classycle.dependency.DependencyChecker
        def main() = TrapExit(
          DependencyChecker.main(Array("-dependencies=@devel/depend.ddf",
                                       classes.toString)),
          s.log)
        def test() = TrapExit(
          DependencyChecker.main(Array("-dependencies=@devel/depend.ddf",
                                       testClasses.toString)),
          s.log)
        main() match {
          case 0 => test() match { case 0 => ; case fail => sys.error(fail.toString) }
          case fail => sys.error(fail.toString)
        }
      }.dependsOn(compile in Test)

  private def ddfContents: String = {
    val buf = new StringBuilder
    def println(s: String) { buf ++= s + "\n" }

    // this needs to be manually kept in sync with devel/depend.graffle
    val packageDefs = Map(
      "" -> Nil,
      "agent" -> List("api"),
      "api" -> List("util"),
      "app" -> List("window"),
      "awt" -> Nil,
      "compiler" -> List("prim","prim/dead","prim/threed"),
      "editor" -> Nil,
      "generator" -> List("prim","prim/dead","prim/threed"),
      "gl/render" -> List("shape"),
      "gl/view" -> List("gl/render","window"),
      "headless" -> List("shape","workspace"),
      "headless/hubnet" -> List("headless", "hubnet/protocol"),
      "hubnet/client" -> List("hubnet/connection","hubnet/mirroring","hubnet/protocol","render","widget"),
      "hubnet/connection" -> List("api"),
      "hubnet/mirroring" -> List("api"),
      "hubnet/protocol" -> List("api"),
      "hubnet/server" -> List("workspace","hubnet/connection","hubnet/mirroring","hubnet/protocol"),
      "hubnet/server/gui" -> List("hubnet/server","window"),
      "job" -> List("nvm"),
      "lab" -> List("nvm"),
      "lab/gui" -> List("lab","window"),
      "lex" -> List("api"),
      "lite" -> List("window"),
      "log" -> List("api"),
      "nvm" -> List("agent"),
      "plot" -> List("api"),
      "prim" -> List("nvm"),
      "prim/dead" -> List("nvm"),
      "prim/etc" -> List("nvm"),
      "prim/file" -> List("nvm"),
      "prim/gui" -> List("window"),
      "prim/hubnet" -> List("nvm"),
      "prim/plot" -> List("nvm","plot"),
      "prim/threed" -> List("nvm"),
      "properties" -> List("window"),
      "render" -> List("shape"),
      "sdm" -> List("api"),
      "sdm/gui" -> List("sdm","window"),
      "shape" -> List("api"),
      "shape/editor" -> List("shape","swing"),
      "swing" -> List("awt"),
      "util" -> Nil,
      "widget" -> List("window"),
      "window" -> List("editor","log","shape","swing","workspace"),
      "workspace" -> List("nvm", "plot"))
    case class Package(val dir: String, var depends: Set[Package]) {
      def ancestors:Set[Package] = depends ++ depends.flatMap(_.ancestors)
    }
    val allPackages: Set[Package] = Set() ++ packageDefs.keys.map(Package(_,Set()))
    for(p <- allPackages)
      p.depends = allPackages.filter(p2 => packageDefs(p.dir).contains(p2.dir))
    def generate(p: Package) {
      val name = p.dir.replaceAll("/",".")
      println("[" + name + "] = org.nlogo." + name + ".* excluding org.nlogo." + name + ".*.*")
      println("[" + name + "+] = [" + name + "]" + p.depends.map(p2 => "[" + p2.dir.replaceAll("/",".") + "+]").mkString(" "," ",""))
      println("[" + name + "-] = org.nlogo.* excluding [" + name + "+]")
      println("check [" + name + "] independentOf [" + name + "-]")
      println("")
    }
    def generateFooter() {
      println("""
### HubNet client dependencies (keep HubNet.jar small!)

[HubNet-client] = [hubnet.client] [hubnet.connection] [hubnet.mirroring] [hubnet.protocol] excluding org.nlogo.hubnet.client.App org.nlogo.hubnet.client.App$ org.nlogo.hubnet.client.ClientApp
check [HubNet-client] independentOf [workspace]
# Someday this should be completely independent, not just directly independent - ST 12/4/08
check [HubNet-client] directlyIndependentOf [nvm]

### checks for packages with only one direct parent

[not-job-not-workspace] = org.nlogo.* excluding [job] [workspace]
check [not-job-not-workspace] directlyIndependentOf [job]

### checks on AWT, Swing, JOGL

[Sun-Swing] = javax.swing.* excluding javax.swing.tree.MutableTreeNode javax.swing.tree.DefaultMutableTreeNode
[Sun-AWT] = java.awt.*
[headless-AWT] = java.awt.geom.* java.awt.image.* java.awt.Color java.awt.Image java.awt.Shape java.awt.Graphics2D java.awt.Graphics java.awt.Stroke java.awt.Composite java.awt.BasicStroke java.awt.Point java.awt.Font java.awt.AlphaComposite java.awt.RenderingHints java.awt.Rectangle java.awt.FontMetrics java.awt.color.ColorSpace java.awt.Polygon java.awt.RenderingHints$Key
# as a special case, we allow referring to java.awt.Frame, because ShapesManagerFactory
# mentions it in its constructor, and I don't want to have to make a whole new package
# just to put ShapesManagaerFactory in - ST 2/27/09
[bad-AWT] = java.awt.* excluding [headless-AWT] java.awt.Frame

check [util+] independentOf [Sun-AWT]
check [awt+] independentOf [Sun-Swing]
check [headless+] independentOf [Sun-Swing] [bad-AWT]
check [gl.render] independentOf [Sun-Swing] [bad-AWT]

### checks on external libraries

[JOGL-free-zone] = org.nlogo.* excluding [gl.render] [gl.view]
[JOGL] = net.java.games.* javax.media.opengl.*
check [JOGL-free-zone] independentOf [JOGL]

[ASM-free-zone] = org.nlogo.* excluding [generator]
check [ASM-free-zone] independentOf org.objectweb.*

check org.nlogo.* independentOf com.wolfram.*

[MRJAdapter-free-zone] = org.nlogo.* excluding [app] [hubnet.client] [swing]
check [MRJAdapter-free-zone] directlyIndependentOf net.roydesign.*

[JHotDraw-free-zone] = org.nlogo.* excluding [sdm.gui]
check [JHotDraw-free-zone] independentOf org.jhotdraw.*

[JMF-free-zone] = org.nlogo.* excluding org.nlogo.awt.JMFMovieEncoder org.nlogo.awt.JMFMovieEncoderDataStream org.nlogo.awt.JMFMovieEncoderDataSource
[JMF] = javax.media.* excluding javax.media.opengl.*
check [JMF-free-zone] directlyIndependentOf [JMF]

[Log4J-free-zone] = org.nlogo.* excluding [log] org.nlogo.app.App org.nlogo.lite.InterfaceComponent
check [Log4J-free-zone] directlyIndependentOf org.apache.log4j.*

[Quaqua-free-zone] = org.nlogo.* excluding org.nlogo.swing.Utils
check [Quaqua-free-zone] directlyIndependentOf ch.randelshofer.*

[PicoContainer-free-zone] = org.nlogo.* excluding org.nlogo.util.Pico [app] [headless]
check [PicoContainer-free-zone] independentOf org.picocontainer.*

"""
              )
    }

    var done = List(allPackages.find(_.dir == "").get)
    def eligible(p:Package) = !done.contains(p) && p.ancestors.forall(done.contains(_))
    while(true) {
      allPackages.filter(!_.dir.isEmpty).find(eligible) match {
        case None =>
          generateFooter()
          return buf.toString
        case Some(p) =>
          generate(p)
          done = p :: done
      }
    }
    throw new IllegalStateException  // unreachable
  }
}

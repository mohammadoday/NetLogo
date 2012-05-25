import java.io.File
import sbt._
import Keys._

trait Extensions {

  val extensions =
    baseDirectory map { base =>
      val isDirectory = new java.io.FileFilter {
        override def accept(f: File) = f.isDirectory
      }
      val dirs = IO.listFiles(isDirectory)(base / "extensions")
      dirs.flatMap(buildExtension)
    }

  private def buildExtension(dir: File): Seq[File] = Nil

}
    
/*
# most of them use NetLogoLite.jar, but the profiler extension uses NetLogo.jar - ST 5/11/11
$(EXTENSIONS) $(EXTENSIONS_PACK200): | NetLogo.jar NetLogoLite.jar
	git submodule update --init
	@echo "@@@ building" $(notdir $@)
	cd $(dir $@); JAVA_HOME=$(JAVA_HOME) SCALA_JAR=../../$(SCALA_JAR_BASE) make -s $(notdir $@)
*/

import sbt._
import Keys._

object NetLogoBuild extends Build {
  lazy val root =
    Project(
      id = "NetLogo",
      base = file("."),
      settings = Defaults.defaultSettings ++ Seq(
        sourceGenerators in Compile <+=
          (javaSource in Compile, baseDirectory, streams) map {
            (dir, base, s) =>
              Seq(Autogen.events(s.log.info(_), base, dir, "window"),
                  Autogen.events(s.log.info(_), base, dir, "app"),
                  Autogen.flex(s.log.info(_), base, dir, "agent", "ImportLexer"),
                  Autogen.flex(s.log.info(_), base, dir, "lex", "TokenLexer"))}
        ))
}

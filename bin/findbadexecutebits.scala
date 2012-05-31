#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

/// Makes sure no files have their execute bit wrongly set or unset

import Scripting.shell

def skip(path:String):Boolean =
  path.containsSlice("/.git/") ||
  path.containsSlice("/tmp/") ||
  path.endsWith("~") ||
  path.endsWith("/.DS_Store") ||
  path.startsWith(".#") ||
  path.containsSlice("/.idea/") ||
  path.containsSlice("/project/build/") ||
  path.containsSlice("/target/")

val executableExtensions =
  "sh py exe"
val executableFullNames =
  "MacOS/HubNet MacOS/NetLogo bin/scala bin/scalac bin/sbt bin/scaladoc"
val nonExecutableExtensions =
  "java nlogo nlogo3d txt flex class classpath srcs css graph srcs-scala prefs launch scala xml png tgz " +
  "nim graffle html diff example xls ddf jar mk doc zip gif bz2 dtd ninfo project versioned log " +
  "ico install4j icns xsl plist jpg wrl dbf shp nb pdf prj shx sample gm m nls asc csv hs fbp " +
  "aif au ogg wav dat bsearch mf properties nbm java_NEW psd iml gitignore ensime sbt md " +
  "dll so jnilib js inf"
val nonExecutableFullNames =
  "README PkgInfo Makefile COPYING .gitmodules"

for{path <- shell("find . -type f -perm +0100"); if !skip(path)} {
  if(executableExtensions.split(" ").forall(ext => !path.endsWith("." + ext)) &&
     (!path.endsWith(".scala") || !path.startsWith("./bin/")) &&
     (!path.endsWith(".scala") || !path.startsWith("./models/bin/")) &&
     !path.startsWith("./scala/bin/") &&
     executableFullNames.split(" ").forall(ext => !path.endsWith("/" + ext)))
    println(path)
}
for{path <- shell("find . -type f -perm +0100 -prune -o -type f -print"); if !skip(path)} {
  if(nonExecutableExtensions.split(" ").forall(ext => !path.endsWith("." + ext)) &&
     nonExecutableFullNames.split(" ").forall(ext => !path.endsWith("/" + ext)) &&
     !path.startsWith("./scala/var/"))
    println(path)
}

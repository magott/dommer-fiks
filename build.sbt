import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name:="dommer-fiks"

organization := "com.andersen-gott"

libraryDependencies ++=
  Seq(
    "net.databinder" %% "unfiltered" % "0.6.0",
    "net.databinder" %% "unfiltered-filter" % "0.6.0",
    "net.databinder" %% "unfiltered-jetty" % "0.6.0",
    "org.jsoup" % "jsoup" % "1.6.1",
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "org.scalatest" %% "scalatest" % "1.7.1" % "test",
    "com.google.guava" % "guava" % "11.0.2",
    "org.mongodb" %% "casbah" % "3.0.0-M2",
    "bouncycastle" % "bcprov-jdk16" % "140",
    "org.jasypt" % "jasypt" % "1.9.0"
  )
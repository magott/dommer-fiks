import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name:="ofk-fiks"

organization := "no.magott"

libraryDependencies ++=
  Seq(
    "net.databinder" %% "unfiltered" % "0.5.3",
    "net.databinder" %% "unfiltered-filter" % "0.5.3",
    "net.databinder" %% "unfiltered-jetty" % "0.5.3",
    "org.jsoup" % "jsoup" % "1.6.1",
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2"
  )
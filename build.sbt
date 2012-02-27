import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name:="ofk-fiks"

organization := "no.magott"

libraryDependencies ++=
  Seq(
    "net.databinder" %% "unfiltered" % "0.5.3",
    "net.databinder" %% "unfiltered-filter" % "0.5.3",
    "net.databinder" %% "unfiltered-jetty" % "0.5.3",
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "commons-logging" % "commons-logging" % "1.1.1"
  )
import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

name:="dommer-fiks"

scalaVersion:="2.10.0"

organization := "com.andersen-gott"

libraryDependencies ++=
  Seq(
    "net.databinder" %% "unfiltered" % "0.6.7",
    "net.databinder" %% "unfiltered-filter" % "0.6.7",
    "net.databinder" %% "unfiltered-jetty" % "0.6.7",
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "commons-logging" % "commons-logging" % "1.1.1",
    "org.jsoup" % "jsoup" % "1.6.1",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "com.google.guava" % "guava" % "11.0.2",
    "org.mongodb" %% "casbah" % "2.5.0",
    "org.mongodb" % "mongo-java-driver" % "2.10.1",
    "bouncycastle" % "bcprov-jdk16" % "140",
    "org.jasypt" % "jasypt" % "1.9.0",
    "no.arktekk" %% "uri-template" % "1.0.1",
    "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
    "org.json4s" %% "json4s-native" % "3.2.4"
  )
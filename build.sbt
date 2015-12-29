import com.typesafe.sbt.SbtStartScript

SbtStartScript.startScriptForClassesSettings

name:="dommer-fiks"

scalaVersion:="2.11.7"

scalacOptions += "-target:jvm-1.8"

organization := "com.andersen-gott"

libraryDependencies ++=
  Seq(
    "net.databinder" %% "unfiltered" % "0.8.4",
    "net.databinder" %% "unfiltered-filter" % "0.8.4",
    "net.databinder" %% "unfiltered-jetty" % "0.8.4",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "io.argonaut" %% "argonaut" % "6.0.4",
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "commons-logging" % "commons-logging" % "1.1.1",
    "org.jsoup" % "jsoup" % "1.6.1",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "com.google.guava" % "guava" % "11.0.2",
    "org.mongodb" %% "casbah" % "2.8.2",
    "org.mongodb" % "mongo-java-driver" % "2.13.2",
    "bouncycastle" % "bcprov-jdk16" % "140",
    "org.jasypt" % "jasypt" % "1.9.0",
    "org.constretto" %% "constretto-scala" % "1.1",
    "org.apache.poi" % "poi-ooxml" % "3.12-beta1"
  )
organization := "com.karasiq"

name := "mapdb-utils"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.mapdb" % "mapdb" % "1.0.7" % "provided",
  "com.typesafe" % "config" % "1.2.1"
)
    
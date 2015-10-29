organization := "com.github.karasiq"

name := "mapdbutils"

version := "1.1-SNAPSHOT"

isSnapshot := true

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.mapdb" % "mapdb" % "2.0-beta8" % "provided",
  "com.typesafe" % "config" % "1.3.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ ⇒ false }

licenses := Seq("The MIT License" → url("http://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/Karasiq/" + name.value))

pomExtra := <scm>
  <url>git@github.com:Karasiq/{name.value}.git</url>
  <connection>scm:git:git@github.com:Karasiq/{name.value}.git</connection>
</scm>
  <developers>
    <developer>
      <id>karasiq</id>
      <name>Piston Karasiq</name>
      <url>https://github.com/Karasiq</url>
    </developer>
  </developers>
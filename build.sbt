import sbt.Keys._

val commonSettings = Seq(
  organization := "com.github.karasiq",
  version := "1.2.0-SNAPSHOT",
  isSnapshot := version.value.endsWith("SNAPSHOT"),
  scalaVersion := "2.11.8",
  // crossScalaVersions := Seq("2.11.8", "2.12.1"), // TODO: https://github.com/jankotek/mapdb/issues/795
  libraryDependencies ++= Seq(
    "org.mapdb" % "mapdb" % "3.0.3"
  ),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ ⇒ false },
  licenses := Seq("The MIT License" → url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/Karasiq/mapdbutils")),
  pomExtra := <scm>
    <url>git@github.com:Karasiq/mapdbutils.git</url>
    <connection>scm:git:git@github.com:Karasiq/mapdbutils.git</connection>
  </scm>
    <developers>
      <developer>
        <id>karasiq</id>
        <name>Piston Karasiq</name>
        <url>https://github.com/Karasiq</url>
      </developer>
    </developers>
)

val rootSettings = Seq(
  name := "mapdbutils",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.1",
    "eu.timepit" %% "refined" % "0.7.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
)

val macroSettings = Seq(
  name := "mapdbutils-macro",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
)

lazy val macros = Project("mapdbutils-macro", new File("macro"))
  .settings(commonSettings, macroSettings)

lazy val root = Project("mapdbutils", new File("."))
  .aggregate(macros)
  .dependsOn(macros)
  .settings(commonSettings, rootSettings)
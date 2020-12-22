import Dependencies._

lazy val `maven-search` =
  (project in file("."))
    .aggregate(logging)
    .dependsOn(logging)
    .enablePlugins(JavaAppPackaging)
    .settings(
      name := "maven-search",
      common,
      deployment,
      libraryDependencies ++= Seq(scopt) ++ scalatest ++ jline.jna ++ circe ++ http4s.client
    )

lazy val logging = (project in file("logging"))
  .settings(
    name := "logging",
    common,
    libraryDependencies ++= odin
  )

lazy val common = Seq(
  maintainer := "saitkocatas@gmail.com",
  organization := "com.deliganli",
  version := "0.1.1",
  scalaVersion := "2.13.4",
  scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-language:higherKinds", "-language:postfixOps", "-feature"),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.2" cross CrossVersion.full)
)

lazy val deployment = Seq(
  maintainer := "Sait Sami Kocatas",
  packageSummary := "Command line program searches given query on maven",
  executableScriptName := "mvns",
  mappings in Universal += (resourceDirectory in Compile).value / "reference.conf" -> "conf/reference.conf"
)

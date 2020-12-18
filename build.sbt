import Dependencies._

lazy val `maven-search` =
  (project in file("."))
    .aggregate(logging)
    .dependsOn(logging)
    .enablePlugins(UniversalPlugin, JvmPlugin)
    .settings(
      name := "maven-search",
      common,
      Deployment.assemblySettings,
      libraryDependencies ++= Seq(
        scopt,
        "net.java.dev.jna" % "jna" % "5.6.0"
        //"org.fusesource.jansi" % "jansi" % "1.18"
      ) ++ Seq(
        "org.jline" % "jline-terminal"     % Versions.jline,
        "org.jline" % "jline-reader"       % Versions.jline,
        "org.jline" % "jline-console"      % Versions.jline,
        "org.jline" % "jline-terminal-jna" % Versions.jline
        //"org.jline" % "jline-terminal-jansi" % Versions.jline
      ) ++ Seq(
        "io.circe" %% "circe-core" % Versions.circe,
        circeConfig,
        http4sCirce
      )
        ++ http4sClient
        ++ scalatest
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
  version := "0.1.0",
  scalaVersion := "2.13.3",
  scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-language:higherKinds", "-language:postfixOps", "-feature"),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
)

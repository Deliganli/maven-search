import com.typesafe.sbt.SbtNativePackager.autoImport._
import sbt.Keys._
import sbt.{CrossVersion, addCompilerPlugin, _}

object Settings {

  val CCTT = "compile->compile;test->test"

  lazy val scala212               = "2.12.8"
  lazy val scala213               = "2.13.3"
  lazy val supportedScalaVersions = List(scala212, scala213)

  lazy val common = Seq(
    maintainer := "saitkocatas@gmail.com",
    organization := "com.deliganli",
    version := "0.1.0",
    scalaVersion := scala213,
    scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-language:higherKinds", "-language:postfixOps", "-feature"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
  )
}

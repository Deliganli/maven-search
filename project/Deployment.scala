import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object Deployment {

  lazy val settings = Seq(
    topLevelDirectory := None,
    javaOptions in Universal ++= Seq(
      "-Dconfig.trace=loads"
    )
  ) ++ assemblySettings

  lazy val assemblySettings = Seq(
    mappings in (Compile, packageDoc) := Seq(),
    test in assembly := {},
    assemblyJarName in assembly := name.value + ".jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "application.conf"            => MergeStrategy.concat
      case x                             => MergeStrategy.first
    }
  ) ++ fatJarSettings

  lazy val fatJarSettings = Seq(
    mappings in Universal := {
      val fatJar   = (assembly in Compile).value
      val filtered = (mappings in Universal).value.filter { case (file, name) => !name.endsWith(".jar") }

      filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    },
    scriptClasspath := Seq((assemblyJarName in assembly).value)
  )

}

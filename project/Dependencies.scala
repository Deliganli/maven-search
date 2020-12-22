import sbt._

object Dependencies {

  object Versions {
    val scalatest   = "3.2.3"
    val circe       = "0.13.0"
    val circeConfig = "0.8.0"
    val mockito     = "1.16.3"
    val http4s      = "0.21.14"
    val scopt       = "4.0.0"
    val odin        = "0.9.1"
    val jline       = "3.18.0"
    val jna         = "5.6.0"
    val jansi       = "1.18"
  }

  val circe = Seq(
    "io.circe" %% "circe-core"   % Versions.circe,
    "io.circe" %% "circe-config" % Versions.circeConfig
  )

  val scalatest = Seq(
    "org.scalactic" %% "scalactic"               % Versions.scalatest,
    "org.scalatest" %% "scalatest"               % Versions.scalatest % Test,
    "org.mockito"   %% "mockito-scala"           % Versions.mockito   % Test,
    "org.mockito"   %% "mockito-scala-cats"      % Versions.mockito   % Test,
    "org.mockito"   %% "mockito-scala-scalatest" % Versions.mockito   % Test
  )

  object http4s {

    def client =
      Seq(
        "org.http4s" %% "http4s-blaze-client",
        "org.http4s" %% "http4s-circe"
      ).map(_ % Versions.http4s)
  }

  val odin = Seq(
    "com.github.valskalla" %% "odin-core",
    "com.github.valskalla" %% "odin-slf4j"
  ).map(_ % Versions.odin)

  object jline {

    val base = Seq(
      "org.jline" % "jline-terminal" % Versions.jline,
      "org.jline" % "jline-reader"   % Versions.jline,
      "org.jline" % "jline-console"  % Versions.jline
    )

    def jna =
      base ++ Seq(
        "org.jline"        % "jline-terminal-jna" % Versions.jline,
        "net.java.dev.jna" % "jna"                % Versions.jna
      )

    def jansi =
      base ++ Seq(
        "org.jline"            % "jline-terminal-jansi" % Versions.jline,
        "org.fusesource.jansi" % "jansi"                % Versions.jansi
      )
  }

  val scopt = "com.github.scopt" %% "scopt" % Versions.scopt
}

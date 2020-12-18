import sbt._

object Dependencies {

  object Versions {
    val scalatest       = "3.2.0"
    val cats            = "2.2.0"
    val osLib           = "0.3.0"
    val circeFs2        = "0.13.0"
    val circe           = "0.13.0"
    val circeConfig     = "0.7.0"
    val fs2             = "2.1.0"
    val enumeratum      = "1.5.15"
    val enumeratumCirce = "1.5.21"
    val breeze          = "1.0"
    val shapeless       = "2.3.3"
    val mockito         = "1.16.0"
    val jodaDateTime    = "2.10.3"
    val http4s          = "0.21.7"
    val http4sJdk       = "0.3.1"
    val scopt           = "4.0.0-RC2"
    val jsoup           = "1.13.1"
    val doobie          = "0.9.0"
    val odin            = "0.9.1"
    val flyway          = "6.2.3"
    val tsec            = "0.2.0"
    val awsS3           = "2.13.10"
    val monocle         = "2.0.0"
    val gcVision        = "1.99.3"
    val gcStorage       = "1.107.0"
    val pdfbox          = "2.0.19"
    val jbig2           = "3.0.3"
    val itext           = "7.1.11"
    val jline           = "3.16.0"
  }

  val circe = Seq(
    "io.circe" %% "circe-core"           % Versions.circe,
    "io.circe" %% "circe-generic"        % Versions.circe,
    "io.circe" %% "circe-generic-extras" % Versions.circe,
    "io.circe" %% "circe-parser"         % Versions.circe,
    "io.circe" %% "circe-shapes"         % Versions.circe
  )

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core"      % Versions.doobie,
    "org.tpolecat" %% "doobie-hikari"    % Versions.doobie,
    "org.tpolecat" %% "doobie-quill"     % Versions.doobie,
    "org.tpolecat" %% "doobie-scalatest" % Versions.doobie % "test"
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core"             % Versions.fs2,
    "co.fs2" %% "fs2-io"               % Versions.fs2,
    "co.fs2" %% "fs2-reactive-streams" % Versions.fs2,
    "co.fs2" %% "fs2-experimental"     % Versions.fs2
  )

  val enumeratum = Seq(
    "com.beachape" %% "enumeratum"       % Versions.enumeratum,
    "com.beachape" %% "enumeratum-circe" % Versions.enumeratumCirce
  )

  val breeze = Seq(
    "org.scalanlp" %% "breeze" % Versions.breeze
    // "org.scalanlp" %% "breeze-natives" % Versions.breeze
  )

  val scalatest = Seq(
    "org.scalactic" %% "scalactic"               % Versions.scalatest,
    "org.scalatest" %% "scalatest"               % Versions.scalatest % Test,
    "org.mockito"   %% "mockito-scala"           % Versions.mockito   % Test,
    "org.mockito"   %% "mockito-scala-cats"      % Versions.mockito   % Test,
    "org.mockito"   %% "mockito-scala-scalatest" % Versions.mockito   % Test
  )

  val cats = Seq(
    "org.typelevel" %% "cats-core",
    "org.typelevel" %% "cats-effect"
  ).map(_ % Versions.cats)

  val http4sClient = Seq(
    "org.http4s" %% "http4s-blaze-client"    % Versions.http4s,
    "org.http4s" %% "http4s-jdk-http-client" % Versions.http4sJdk
  )

  val jline = Seq(
    "org.jline" % "jline-terminal"     % Versions.jline,
    "org.jline" % "jline-reader"       % Versions.jline,
    "org.jline" % "jline-console"      % Versions.jline,
    "org.jline" % "jline-terminal-jna" % Versions.jline,
    "org.jline" % "jline-terminal-jansi" % Versions.jline
  )

  val http4sServer = Seq(
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-blaze-server"
  ).map(_ % Versions.http4s)

  val http4sCirce = "org.http4s" %% "http4s-circe" % Versions.http4s

  val odin = Seq(
    "com.github.valskalla" %% "odin-core",
    "com.github.valskalla" %% "odin-json",
    "com.github.valskalla" %% "odin-extras",
    "com.github.valskalla" %% "odin-slf4j"
  ).map(_ % Versions.odin)

  val tsec = Seq(
    "io.github.jmcardon" %% "tsec-common",
    "io.github.jmcardon" %% "tsec-password",
    "io.github.jmcardon" %% "tsec-cipher-jca",
    "io.github.jmcardon" %% "tsec-cipher-bouncy",
    "io.github.jmcardon" %% "tsec-mac",
    "io.github.jmcardon" %% "tsec-signatures",
    "io.github.jmcardon" %% "tsec-hash-jca",
    "io.github.jmcardon" %% "tsec-hash-bouncy",
    //"io.github.jmcardon" %% "tsec-libsodium",
    "io.github.jmcardon" %% "tsec-jwt-mac",
    "io.github.jmcardon" %% "tsec-jwt-sig",
    "io.github.jmcardon" %% "tsec-http4s"
  ).map(_ % Versions.tsec)

  val pdfbox = Seq(
    "org.apache.pdfbox" % "pdfbox"
  ).map(_               % Versions.pdfbox) ++ Seq(
    "org.apache.pdfbox" % "jbig2-imageio" % Versions.jbig2
  )

  val itext = Seq(
    "com.itextpdf" % "kernel", //  always needed
    "com.itextpdf" % "io", //  always needed
    "com.itextpdf" % "layout", //   always needed
    "com.itextpdf" % "forms", //  only needed for forms
    "com.itextpdf" % "pdfa", //  only needed for PDF/A
    "com.itextpdf" % "sign", //  only needed for digital signatures
    "com.itextpdf" % "barcodes", //  only needed for barcodes
    "com.itextpdf" % "font-asian", //  only needed for Asian fonts
    "com.itextpdf" % "hyph" //  only needed for hyphenation
  ).map(_ % Versions.itext)

  val opencv = Seq(
    //"org.openpnp"  % "opencv"          % "4.3.0-2",
    "org.bytedeco" % "javacv-platform" % "1.5.3"
  )

  val monocle = Seq(
    "com.github.julien-truffaut" %% "monocle-core"  % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-macro" % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-law"   % Versions.monocle % "test"
  )

  val sqlite       = "org.xerial"             % "sqlite-jdbc"          % "3.32.3.2"
  val postgres     = "org.tpolecat"          %% "doobie-postgres"      % Versions.doobie
  val gcVision     = "com.google.cloud"       % "google-cloud-vision"  % Versions.gcVision
  val gcStorage    = "com.google.cloud"       % "google-cloud-storage" % Versions.gcStorage
  val awsS3        = "software.amazon.awssdk" % "s3"                   % Versions.awsS3
  val tesseract    = "net.sourceforge.tess4j" % "tess4j"               % "4.5.1"
  val flyway       = "org.flywaydb"           % "flyway-core"          % Versions.flyway
  val circeFs2     = "io.circe"              %% "circe-fs2"            % Versions.circeFs2
  val circeConfig  = "io.circe"              %% "circe-config"         % Versions.circeConfig
  val osLib        = "com.lihaoyi"           %% "os-lib"               % Versions.osLib
  val shapeless    = "com.chuusai"           %% "shapeless"            % Versions.shapeless
  val jodaTime     = "joda-time"              % "joda-time"            % Versions.jodaDateTime
  val scopt        = "com.github.scopt"      %% "scopt"                % Versions.scopt
  val jsoup        = "org.jsoup"              % "jsoup"                % Versions.jsoup
  val console4cats = "dev.profunktor"        %% "console4cats"         % "0.8.1"
}

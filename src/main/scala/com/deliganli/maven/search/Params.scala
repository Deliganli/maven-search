package com.deliganli.maven.search

import cats.effect.Sync
import com.deliganli.maven.search.Domain.ImportFormat
import com.deliganli.maven.search.Domain.ImportFormat.{Maven, Sbt}
import scopt.Read

case class Params(
  query: String = "",
  importFormat: Option[ImportFormat] = None,
  copyToClipboard: Option[Boolean] = None,
  debug: Option[Boolean] = None)

object Params {
  import scopt.OParser
  val builder = OParser.builder[Params]

  implicit val read: Read[ImportFormat] = Read.reads {
    case "sbt"   => new Sbt()
    case "maven" => new Maven()
  }

  val paramParser = {
    import builder._
    OParser.sequence(
      programName("mvns"),
      head("maven search", "0.1"),
      arg[String]("<query string>")
        .action((x, p) => p.copy(query = x))
        .text("Query string to be searched in maven central"),
      opt[Boolean]("no-copy")
        .optional()
        .action((x, p) => p.copy(copyToClipboard = Some(x)))
        .text("Don't copy to clipboard, disables selection altogether"),
      opt[ImportFormat]('f', "import-format")
        .optional()
        .action((x, p) => p.copy(importFormat = Some(x)))
        .text("Format to be copied to clipboard, e.g. sbt"),
      opt[Boolean]('d', "debug")
        .optional()
        .action((x, p) => p.copy(debug = Some(x)))
        .text("Debug mode")
    )
  }

  def parse[F[_]: Sync](args: List[String]): F[Option[Params]] = Sync[F].delay(OParser.parse(paramParser, args, Params()))
}

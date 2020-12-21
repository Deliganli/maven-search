package com.deliganli.maven.search

import cats.implicits._
import com.deliganli.maven.search.Domain.ImportFormat
import org.http4s.Uri
import scopt.{OParser, Read}

package object cmd {
  val builder = OParser.builder[Params]

  implicit val importFormat: Read[ImportFormat] = Read.reads {
    case "sbt"     => ImportFormat.Sbt
    case "generic" => ImportFormat.Generic
  }

  implicit val uri: Read[Uri] = Read.reads(Uri.unsafeFromString)

  val paramParser: OParser[Unit, Params] = {
    import builder._
    OParser.sequence(
      programName("mvns"),
      head("maven search", "0.1"),
      opt[Uri]("maven-uri")
        .optional()
        .action((x, p) => p.copy(mavenUri = x.some))
        .text("Use this uri to perform maven style searches"),
      opt[Int]("chunk-size")
        .optional()
        .action((x, p) => p.copy(chunkSize = x.some))
        .text("Amount of the elements that will be requested on each rest call"),
      opt[Int]("item-per-page")
        .optional()
        .action((x, p) => p.copy(itemPerPage = x.some))
        .text("Amount if items to show on each page"),
      opt[ImportFormat]('f', "import-format")
        .optional()
        .action((x, p) => p.copy(importFormat = x.some))
        .text("Format to be copied to clipboard, e.g. sbt"),
      opt[Boolean]('d', "debug")
        .optional()
        .action((x, p) => p.copy(debug = x.some))
        .text("Debug mode"),
      opt[Boolean]('c', "clear-screen")
        .optional()
        .action((x, p) => p.copy(clearScreen = x.some))
        .text("Clear screen on each navigation activity"),
      arg[String]("<query string>")
        .action((x, p) => p.copy(query = x))
        .text("Query string to be searched in maven central")
    )
  }
}

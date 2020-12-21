package com.deliganli.maven.search

import cats.effect.Sync
import com.deliganli.maven.search.Domain.ImportFormat
import com.deliganli.maven.search.cmd._
import org.http4s.Uri
import scopt.OParser

case class Params(
  query: String,
  mavenUri: Option[Uri],
  chunkSize: Option[Int],
  itemPerPage: Option[Int],
  importFormat: Option[ImportFormat],
  debug: Option[Boolean],
  clearScreen: Option[Boolean])

object Params {

  private def initializer = Params("", None, None, None, None, None, None)

  def parse[F[_]: Sync](args: List[String]): F[Option[Params]] = Sync[F].delay(OParser.parse(paramParser, args, initializer))

  case class Config(
    query: String,
    mavenUri: Uri,
    chunkSize: Int,
    itemPerPage: Int,
    importFormat: ImportFormat,
    debug: Boolean,
    clearScreen: Boolean)

  def merge(params: Params, config: Config): Config = {
    Config(
      query = params.query,
      mavenUri = params.mavenUri.getOrElse(config.mavenUri),
      chunkSize = params.chunkSize.getOrElse(config.chunkSize),
      itemPerPage = params.itemPerPage.getOrElse(config.itemPerPage),
      importFormat = params.importFormat.getOrElse(config.importFormat),
      debug = params.debug.getOrElse(config.debug),
      clearScreen = params.clearScreen.getOrElse(config.clearScreen)
    )
  }
}

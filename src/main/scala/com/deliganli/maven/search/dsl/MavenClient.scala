package com.deliganli.maven.search.dsl

import cats.effect.Sync
import com.deliganli.maven.search.Domain.MavenModel
import com.deliganli.maven.search.Params.Config
import com.deliganli.maven.search.json._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client

trait MavenClient[F[_]] {
  def search(page: Int): F[MavenModel]
}

object MavenClient {

  object Query {

    def search(
      uri: Uri,
      query: String,
      start: Int,
      rows: Int
    ) =
      uri
        .withQueryParam("start", start)
        .withQueryParam("rows", rows)
        .withQueryParam("q", query)

    def versions(uri: Uri, g: String, a: String) =
      uri
        .withQueryParam("rows", 9)
        .withQueryParam("q", s"g:$g AND a:$a")
        .withQueryParam("core", "gav")
  }

  def dsl[F[_]: Sync](httpClient: Client[F], config: Config): MavenClient[F] = {
    new MavenClient[F] {
      override def search(page: Int): F[MavenModel] = {
        val start  = (page - 1) * config.itemPerPage
        val query  = Query.search(config.mavenUri, config.query, start, config.chunkSize)
        val result = httpClient.get(query)(_.as[MavenModel])

        result
      }
    }
  }
}

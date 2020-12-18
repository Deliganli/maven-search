package com.deliganli.maven.search

import cats.effect.Sync
import com.deliganli.maven.search.Domain.{Config, MavenModel}
import com.deliganli.maven.search.circe._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client

trait MavenClient[F[_]] {
  def search(page: Int): F[MavenModel]
}

object MavenClient {

  def dsl[F[_]: Sync](httpClient: Client[F], params: Params, config: Config): MavenClient[F] = {
    new MavenClient[F] {
      override def search(page: Int): F[MavenModel] = {
        val start  = (page - 1) * config.itemPerPage
        val query  = Query.search(config.mavenUri, params.query, start, config.chunkSize)
        val result = httpClient.get(query)(_.as[MavenModel])

        result
      }
    }
  }
}

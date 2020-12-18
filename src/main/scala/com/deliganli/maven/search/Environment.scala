package com.deliganli.maven.search

import java.net.http.HttpClient
import java.time.Duration

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import cats.implicits._
import com.deliganli.core.logging.PresetLogger
import com.deliganli.maven.search.Domain.Config
import com.deliganli.maven.search.Domain.ImportFormat.Sbt
import com.deliganli.maven.search.circe._
import com.typesafe.config.ConfigFactory
import io.circe.config.parser
import io.odin.Logger
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.client.{middleware, Client}

case class Environment[F[_]](
  params: Params,
  config: Config,
  logger: Logger[F],
  terminal: Terminal[F],
  clipboard: Clipboard[F],
  formatter: Formatter[Sbt],
  maven: MavenClient[F])

object Environment {

  def create[F[_]: ConcurrentEffect: ContextShift: Timer](params: Params): Resource[F, Environment[F]] = {
    for {
      logger      <- PresetLogger.default[F]()
      config      <- Resource.liftF(loadConfig(params))
      clipboard   <- Resource.liftF(Clipboard.system[F])
      mavenClient <- Resource.pure[F, MavenClient[F]](buildMavenClient(logger, config, params))
      terminal    <- Terminal.kek[F]
    } yield Environment(params, config, logger, terminal, clipboard, Formatter.sbt, mavenClient)
  }

  private def loadConfig[F[_]: Sync](params: Params): F[Config] = {
    Sync[F]
      .fromEither {
        val c          = ConfigFactory.load(Main.getClass.getClassLoader)
        val underlying = c.getConfig("maven-search")
        parser.decode[Config](underlying)(config)
      }
      .map { config =>
        Config(
          mavenUri = config.mavenUri,
          chunkSize = config.chunkSize,
          itemPerPage = config.itemPerPage,
          copyToClipboard = params.copyToClipboard.getOrElse(config.copyToClipboard),
          importFormat = params.importFormat.getOrElse(config.importFormat),
          debug = params.debug.getOrElse(config.debug)
        )
      }
  }

  private def defaultHttpClientBuilder[F[_]: ConcurrentEffect: ContextShift] = {
    HttpClient
      .newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .connectTimeout(Duration.ofSeconds(60))
  }

  private def jdkHttpClient[F[_]: ConcurrentEffect: ContextShift]: Client[F] = {
    JdkHttpClient(defaultHttpClientBuilder.build())
  }

  private def loggingJdkHttpClient[F[_]: ConcurrentEffect: ContextShift](L: Logger[F]): Client[F] = {
    val logger = (s: String) => L.debug(s)

    middleware.Logger(logHeaders = true, logBody = true, logAction = logger.some)(jdkHttpClient)
  }

  private def buildMavenClient[F[_]: ConcurrentEffect: ContextShift: Timer](
    logger: Logger[F],
    config: Config,
    params: Params
  ): MavenClient[F] = {
    val client      = if (config.debug) loggingJdkHttpClient[F](logger) else jdkHttpClient[F]
    val mavenClient = MavenClient.dsl(client, params, config)

    mavenClient
  }

}

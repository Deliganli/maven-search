package com.deliganli.maven.search

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import cats.implicits._
import com.deliganli.maven.search.Params.Config
import com.deliganli.maven.search.dsl._
import com.deliganli.maven.search.json._
import com.deliganli.maven.search.logging.PresetLogger
import com.typesafe.config.ConfigFactory
import io.circe.config.parser
import io.odin.Logger
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.{middleware, Client}

import scala.concurrent.ExecutionContext

case class Environment[F[_]](
  config: Config,
  logger: Logger[F],
  terminal: Terminal[F],
  clipboard: Clipboard[F],
  eventMapper: EventMapper,
  maven: MavenClient[F])

object Environment {

  def create[F[_]: ConcurrentEffect: ContextShift: Timer](loader: ClassLoader, params: Params): Resource[F, Environment[F]] = {
    for {
      config      <- Resource.liftF[F, Config](loadConfig(loader, params))
      logger      <- buildLogger[F](config)
      clipboard   <- Resource.liftF[F, Clipboard[F]](Clipboard.system[F])
      terminal    <- Terminal.sync[F](config)
      mavenClient <- buildMavenClient(logger, config)
      eventMapper <- Resource.pure[F, EventMapper](EventMapper.dsl(config))
    } yield Environment(config, logger, terminal, clipboard, eventMapper, mavenClient)
  }

  private def buildLogger[F[_]: Concurrent: ContextShift: Timer](config: Config): Resource[F, Logger[F]] = {
    if (config.debug) PresetLogger.default[F]()
    else Resource.pure[F, Logger[F]](Logger.noop[F])
  }

  def loadConfig[F[_]: Sync](classLoader: ClassLoader, params: Params): F[Config] = {
    Sync[F]
      .fromEither(parser.decode[Config](ConfigFactory.load(classLoader).getConfig("maven-search"))(config))
      .map(config => Params.merge(params, config))
  }

  private def loggingJdkHttpClient[F[_]: ConcurrentEffect: ContextShift](L: Logger[F], client: Client[F]): Client[F] = {
    val logger = (s: String) => L.debug(s)

    middleware.Logger(logHeaders = true, logBody = true, logAction = logger.some)(client)
  }

  def buildMavenClient[F[_]: ConcurrentEffect: ContextShift: Timer](
    logger: Logger[F],
    config: Config
  ): Resource[F, MavenClient[F]] = {
    BlazeClientBuilder[F](ExecutionContext.Implicits.global).resource
      .map(client => if (config.debug) loggingJdkHttpClient[F](logger, client) else client)
      .map(client => MavenClient.dsl(client, config))
  }

}

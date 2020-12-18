package com.deliganli.maven.search

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import cats.implicits._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = composition[IO](args)

  def composition[F[_]: ConcurrentEffect: ContextShift: Timer](args: List[String]): F[ExitCode] = {
    Applicative[F].unit
      .flatMap(_ => Params.parse[F](args))
      .flatMap(_.fold(Applicative[F].unit)(ps => Program.instance[F](ps).void))
      .as(ExitCode.Success)
  }
}

package com.deliganli.maven.search

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import cats.implicits._
import cats.{Applicative, Parallel}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = composition[IO](args)

  def composition[F[_]: Parallel: ConcurrentEffect: ContextShift: Timer](args: List[String]): F[ExitCode] = {
    Applicative[F].unit
      .flatMap(_ => Params.parse[F](args))
      .flatMap(_.fold(Applicative[F].unit)(ps => task[F](ps).void))
      .as(ExitCode.Success)
  }

  def task[F[_]: Parallel: ConcurrentEffect: ContextShift: Timer](params: Params): F[Unit] = {
    Environment
      .create[F](getClass.getClassLoader, params)
      .use(Interpreter.task[F])
      .widen
  }
}

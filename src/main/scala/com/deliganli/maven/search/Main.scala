package com.deliganli.maven.search

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync, Timer}
import cats.implicits._
import com.deliganli.maven.search.Program.{ProgramEvent, State}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = composition[IO](args)

  def composition[F[_]: ConcurrentEffect: ContextShift: Timer](args: List[String]): F[ExitCode] = {
    Applicative[F].unit
      .flatMap(_ => Params.parse[F](args))
      .flatMap(_.fold(Applicative[F].unit)(ps => task[F](ps).void))
      .as(ExitCode.Success)
  }

  def task[F[_]: ConcurrentEffect: ContextShift: Timer](params: Params): F[Unit] = {
    Environment
      .create(params)
      .use(entrypoint[F])
      .widen
  }

  def entrypoint[F[_]: Sync](env: Environment[F]): F[Unit] = {
    Program.interpret(env, State(0, Nil))(ProgramEvent.Search(1))
  }

}

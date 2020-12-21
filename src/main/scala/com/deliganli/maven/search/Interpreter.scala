package com.deliganli.maven.search

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import cats.{Applicative, Monad}
import com.deliganli.maven.search.Domain.{ImportFormat, ProgramEvent}
import com.deliganli.maven.search.dsl.ModelOperator.{Generic, ScalaGrouped}
import com.deliganli.maven.search.dsl.{ModelOperator, Program}

object Interpreter {
  case class State[A](page: Int, items: List[A])

  def task[F[_]: ConcurrentEffect: ContextShift: Timer](env: Environment[F]): F[Unit] = {
    env.config.importFormat match {
      case ImportFormat.Sbt =>
        Ref.of(State(0, List.empty[ScalaGrouped])).flatMap { ref =>
          implicit val P: Program[F, ScalaGrouped] = Program.dsl(ref, ModelOperator.sbt, env)
          interpret[F, ScalaGrouped](ProgramEvent.Search(1))
        }

      case _ =>
        Ref.of(State(0, List.empty[Generic])).flatMap { ref =>
          implicit val P: Program[F, Generic] = Program.dsl(ref, ModelOperator.generic, env)
          interpret[F, Generic](ProgramEvent.Search(1))
        }
    }
  }

  def interpret[F[_]: Monad: Program[*[_], A], A](event: ProgramEvent): F[Unit] =
    event match {
      case ProgramEvent.Search(page)    => Program[F, A].search(page) *> interpret[F, A](ProgramEvent.Prompt)
      case ProgramEvent.Move(page)      => Program[F, A].move(page) *> interpret[F, A](ProgramEvent.Prompt)
      case ProgramEvent.Prompt          => Program[F, A].prompt().flatMap(interpret[F, A])
      case ProgramEvent.Copy(selection) => Program[F, A].copy(selection)
      case ProgramEvent.Exit            => Applicative[F].unit
    }
}

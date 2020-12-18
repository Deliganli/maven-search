package com.deliganli.maven.search

import cats.data.Validated
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits._
import cats.{Applicative, Monad}
import com.deliganli.maven.search.Domain.MavenModel
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc
import com.deliganli.maven.search.Program.ProgramEvent._

object Program {

  sealed trait ProgramEvent

  object ProgramEvent {
    case class Search(page: Int)    extends ProgramEvent
    case object Prompt              extends ProgramEvent
    case class Copy(selection: Int) extends ProgramEvent
    case class Move(page: Int)      extends ProgramEvent
    case object Exit                extends ProgramEvent
  }

  sealed trait UserEvent

  object UserEvent {
    case object Next             extends UserEvent
    case object Prev             extends UserEvent
    case class Selection(i: Int) extends UserEvent
  }

  case class State(page: Int, docs: List[MavenDoc])

  def instance[F[_]: ConcurrentEffect: ContextShift: Timer](params: Params): F[Unit] = {
    Environment
      .create(params)
      .use(entrypoint[F])
      .widen
  }

  def entrypoint[F[_]: Sync](env: Environment[F]): F[Unit] = {
    implicit val C: Clipboard[F] = env.clipboard
    implicit val T: Terminal[F]  = env.terminal

    Applicative[F].unit
      .flatMap(_ => Ref.of(State(0, Nil)))
      .flatMap(cache => interpret(env, cache)(ProgramEvent.Search(1)))
  }

  def string2UserEvent(s: String): Validated[String, UserEvent] =
    s match {
      case s if s == "n" || s == "N" => UserEvent.Next.valid
      case s if s == "p" || s == "P" => UserEvent.Prev.valid
      case s                         => s.toIntOption.map(UserEvent.Selection).toValid(s)
    }

  def transformEvent[F[_]](env: Environment[F], state: State)(e: UserEvent): ProgramEvent = {
    def expected: PartialFunction[UserEvent, ProgramEvent] = {
      case UserEvent.Next if state.docs.size > state.page * env.config.itemPerPage => Move(state.page + 1)
      case UserEvent.Next if state.docs.size % env.config.itemPerPage == 0 => Search(state.page + 1)
      case UserEvent.Prev if state.page > 1 => Move(state.page - 1)
      case UserEvent.Selection(i)           => Copy(i)
    }

    expected.applyOrElse[UserEvent, ProgramEvent](e, _ => Exit)
  }

  def interpret[F[_]: Monad](
    env: Environment[F],
    state: Ref[F, State]
  )(
    event: ProgramEvent
  ): F[Unit] = {
    Applicative[F]
      .pure(event)
      .flatTap(e => env.logger.info(e.toString))
      .flatMap {
        case Prompt =>
          def proceed(e: UserEvent): F[ProgramEvent] =
            state.get
              .flatTap(s => env.logger.info(s"size: ${s.docs.size}, page:${s.page}"))
              .map(s => transformEvent(env, s)(e))

          def terminate(e: String): F[ProgramEvent] =
            env.terminal
              .putStrLn(s"$e is invalid")
              .as(Exit)

          Applicative[F].unit
            .flatMap(_ => env.terminal.readChar.map(string2UserEvent))
            .flatMap(_.fold(terminate, proceed))
            .flatMap(e => interpret(env, state)(e))

        case Search(page) =>
          def updateCache(m: MavenModel) =
            state.update { s =>
              val cursor = s.page * env.config.itemPerPage
              if (cursor < s.docs.size) s.copy(page = page)
              else State(page, s.docs ++ m.docs)
            }

          Applicative[F].unit
            .flatMap(_ => env.maven.search(page))
            .flatTap(m => updateCache(m))
            .flatTap(m => env.terminal.printTable(m.docs.take(env.config.itemPerPage), page))
            .flatMap(_ => interpret(env, state)(Prompt))

        case Move(page) =>
          def updateCache(): F[List[MavenDoc]] =
            state.modify { s =>
              val updated = s.copy(page = page)
              val from    = s.page * env.config.itemPerPage
              val till    = from + env.config.itemPerPage

              (updated, s.docs.slice(from, till))
            }

          Applicative[F].unit
            .flatMap(_ => updateCache())
            .flatTap(m => env.terminal.printTable(m, page))
            .flatMap(_ => interpret(env, state)(Prompt))

        case Copy(selection) =>
          Applicative[F].unit
            .flatMap(_ => state.get.map(s => s.docs((s.page - 1) * env.config.itemPerPage + selection)))
            .map(doc => env.formatter.format(doc))
            .flatTap(fs => env.clipboard.set(fs))
            .flatTap(fs => env.terminal.putStrLn(s"Copied to clipboard: $fs"))
            .void

        case Exit =>
          Applicative[F].unit
      }
  }
}

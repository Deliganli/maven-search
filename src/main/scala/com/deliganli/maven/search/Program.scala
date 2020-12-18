package com.deliganli.maven.search

import cats.effect.concurrent.Ref
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

  def interpret[F[_]: Monad](
    env: Environment[F],
    state: Ref[F, State]
  )(
    event: ProgramEvent
  ): F[Unit] = {
    Applicative[F]
      .pure(event)
      .flatTap(e => env.logger.debug(e.toString))
      .flatMap {
        case Prompt =>
          def proceed(e: UserEvent): F[ProgramEvent] =
            state.get
              .flatTap(s => env.logger.debug(s"size: ${s.docs.size}, page:${s.page}"))
              .map(s => env.transformer.userToProgram(s)(e))

          def terminate(e: String): F[ProgramEvent] =
            env.terminal
              .putStrLn(s"$e is invalid")
              .as(Exit)

          Applicative[F].unit
            .flatMap(_ => env.terminal.readChar.map(env.transformer.stringToUserEvent))
            .flatMap(_.fold(terminate, proceed))
            .flatMap(e => interpret(env, state)(e))

        case Search(page) =>
          def updateCache(m: MavenModel): F[Unit] =
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

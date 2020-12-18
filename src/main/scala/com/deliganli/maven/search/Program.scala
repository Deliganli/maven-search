package com.deliganli.maven.search

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
    state: State
  )(
    event: ProgramEvent
  ): F[Unit] = {
    Applicative[F]
      .pure(event)
      .flatTap(e => env.logger.debug(e.toString))
      .flatMap {
        case Prompt =>
          def proceed(e: UserEvent): F[ProgramEvent] =
            Applicative[F].unit
              .flatMap(_ => env.logger.debug(s"size: ${state.docs.size}, page:${state.page}"))
              .map(_ => env.transformer.userToProgram(state)(e))

          def terminate(e: String): F[ProgramEvent] =
            env.terminal
              .putStrLn(s"$e is invalid")
              .as(Exit)

          Applicative[F].unit
            .flatMap(_ => env.terminal.readChar.map(env.transformer.stringToUserEvent))
            .flatMap(_.fold(terminate, proceed))
            .flatMap(e => interpret(env, state)(e))

        case Search(page) =>
          def updateCache(m: MavenModel): State = {
            val cursor = state.page * env.config.itemPerPage
            if (cursor < state.docs.size) state.copy(page = page)
            else State(page, state.docs ++ m.docs)
          }

          Applicative[F].unit
            .flatMap(_ => env.maven.search(page))
            .flatTap(m => env.terminal.printTable(m.docs.take(env.config.itemPerPage), page))
            .map(m => updateCache(m))
            .flatMap(updatedState => interpret(env, updatedState)(Prompt))

        case Move(page) =>
          def updateCache(): (State, List[MavenDoc]) = {
            val updated = state.copy(page = page)
            val from    = state.page * env.config.itemPerPage
            val till    = from + env.config.itemPerPage

            (updated, state.docs.slice(from, till))
          }

          Applicative[F]
            .pure(updateCache())
            .flatTap { case (_, m) => env.terminal.printTable(m, page) }
            .flatMap { case (s, _) => interpret(env, s)(Prompt) }

        case Copy(selection) =>
          Applicative[F]
            .pure(state.docs((state.page - 1) * env.config.itemPerPage + selection))
            .map(doc => env.formatter.format(doc))
            .flatTap(fs => env.clipboard.set(fs))
            .flatTap(fs => env.terminal.putStrLn(s"Copied to clipboard: $fs"))
            .void

        case Exit =>
          Applicative[F].unit
      }
  }
}

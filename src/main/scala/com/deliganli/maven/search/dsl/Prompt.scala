package com.deliganli.maven.search.dsl

import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Applicative, Monad}
import com.deliganli.maven.search.Domain.{ProgramEvent, UserEvent}
import com.deliganli.maven.search.Interpreter.State
import io.odin.Logger

trait Prompt[F[_]] {
  def prompt(): F[ProgramEvent]
}

object Prompt {
  def apply[F[_]](implicit ev: Prompt[F]): Prompt[F] = ev

  def dsl[F[_]: Monad, A](
    ref: Ref[F, State[A]],
    logger: Logger[F],
    terminal: Terminal[F],
    eventMapper: EventMapper
  ): Prompt[F] =
    new Prompt[F] {

      def proceed(e: UserEvent): F[ProgramEvent] =
        Applicative[F].unit
          .flatMap(_ => ref.get)
          .flatTap(state => logger.debug(s"size: ${state.items.size}, page:${state.page}"))
          .map(state => eventMapper.userToProgram(state.items.size, state.page)(e))

      def terminate(e: String): F[ProgramEvent] =
        terminal
          .putStrLn(s"$e is invalid")
          .as(ProgramEvent.Exit)

      override def prompt(): F[ProgramEvent] = {
        Applicative[F].unit
          .flatMap(_ => terminal.readChar.map(eventMapper.stringToUserEvent))
          .flatMap(_.fold(terminate, proceed))
      }
    }
}

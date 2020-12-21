package com.deliganli.maven.search.dsl

import cats.Monad
import cats.effect.concurrent.Ref
import com.deliganli.maven.search.Domain.{ProgramEvent, State}
import com.deliganli.maven.search.Environment

trait Program[F[_], A] {
  def search(page: Int): F[Unit]
  def prompt(): F[ProgramEvent]
  def move(page: Int): F[Unit]
  def copy(selection: Int): F[Unit]
}

object Program {
  def apply[F[_], A](implicit ev: Program[F, A]): Program[F, A] = ev

  def dsl[F[_]: Monad, A](ref: Ref[F, State[A]], operator: ModelOperator[A], env: Environment[F]): Program[F, A] = {
    val S = Search.dsl[F, A](ref, env.config, env.maven, env.terminal, operator)
    val P = Prompt.dsl[F, A](ref, env.logger, env.terminal, env.eventMapper)
    val M = Move.dsl[F, A](ref, env.config, env.terminal, operator)
    val C = Copy.dsl[F, A](ref, env.config, env.clipboard, env.terminal, operator)

    new Program[F, A] {
      def search(page: Int): F[Unit]    = S.search(page)
      def prompt(): F[ProgramEvent]     = P.prompt()
      def move(page: Int): F[Unit]      = M.move(page)
      def copy(selection: Int): F[Unit] = C.copy(selection)
    }
  }
}

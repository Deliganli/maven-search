package com.deliganli.maven.search.dsl

import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Applicative, Monad}
import com.deliganli.maven.search.Interpreter.State
import com.deliganli.maven.search.Params.Config

trait Copy[F[_]] {
  def copy(selection: Int): F[Unit]
}

object Copy {
  def apply[F[_]](implicit ev: Copy[F]): Copy[F] = ev

  def dsl[F[_]: Monad, A](
    ref: Ref[F, State[A]],
    config: Config,
    clipboard: Clipboard[F],
    terminal: Terminal[F],
    pipeline: ModelOperator[A]
  ): Copy[F] =
    new Copy[F] {

      override def copy(selection: Int): F[Unit] = {
        Applicative[F].unit
          .flatMap(_ => ref.get)
          .map(state => state.items((state.page - 1) * config.itemPerPage + (selection - 1)))
          .map(doc => pipeline.format(doc))
          .flatTap(fs => clipboard.set(fs))
          .flatTap(fs => terminal.putStrLn(s"Copied to clipboard: $fs"))
          .void
      }
    }
}

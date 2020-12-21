package com.deliganli.maven.search.dsl

import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Applicative, Monad}
import com.deliganli.maven.search.Domain.State
import com.deliganli.maven.search.Params.Config

trait Move[F[_]] {
  def move(page: Int): F[Unit]
}

object Move {
  def apply[F[_]](implicit ev: Move[F]): Move[F] = ev

  def dsl[F[_]: Monad, A](
    ref: Ref[F, State[A]],
    config: Config,
    terminal: Terminal[F],
    pipeline: ModelOperator[A]
  ): Move[F] =
    new Move[F] {

      override def move(page: Int): F[Unit] = {
        def updateCache(): F[List[A]] =
          ref.modify { state =>
            val updated = state.copy(page = page)
            val from    = (page - 1) * config.itemPerPage
            val till    = from + config.itemPerPage

            (updated, state.items.slice(from, till))
          }

        Applicative[F].unit
          .flatMap(_ => updateCache())
          .flatTap { items =>
            val table = pipeline.modelToTable(items)
            terminal.printTable(table, page, items.size)
          }
          .void
      }
    }
}

package com.deliganli.maven.search.dsl

import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Applicative, Monad}
import com.deliganli.maven.search.Interpreter.State
import com.deliganli.maven.search.Params.Config

trait Search[F[_], A] {
  def search(page: Int): F[Unit]
}

object Search {
  def apply[F[_], A](implicit ev: Search[F, A]): Search[F, A] = ev

  def dsl[F[_]: Monad, A](
    ref: Ref[F, State[A]],
    config: Config,
    maven: MavenClient[F],
    terminal: Terminal[F],
    pipeline: ModelOperator[A]
  ): Search[F, A] = {
    new Search[F, A] {
      override def search(page: Int): F[Unit] = {
        def updateCache(items: List[A]): F[Unit] =
          ref.update { state =>
            val cursor = state.page * config.itemPerPage
            if (cursor < state.items.size) state.copy(page = page)
            else State(page, state.items ++ items)
          }

        Applicative[F].unit
          .flatMap(_ => maven.search(page).map(_.docs))
          .map(docs => pipeline.mavenToModel(docs))
          .flatTap { items =>
            val pageItems = items.take(config.itemPerPage)
            val table     = pipeline.modelToTable(pageItems)
            terminal.printTable(table, page, pageItems.size)
          }
          .flatTap(items => updateCache(items))
          .void
      }
    }
  }
}

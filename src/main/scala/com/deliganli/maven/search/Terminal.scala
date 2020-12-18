package com.deliganli.maven.search

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc
import com.deliganli.maven.search.Visual.{buildTable, message}
import org.jline.terminal
import org.jline.terminal.TerminalBuilder

trait Terminal[F[_]] {
  def readChar: F[String]
  def putStrLn(s: String): F[Unit]
  def printTable(docs: List[MavenDoc], page: Int): F[Unit]
}

object Terminal {

  def apply[F[_]](implicit ev: Terminal[F]) = ev

  def sync[F[_]: Sync]: Resource[F, Terminal[F]] = {
    underlyingTerminalResource
      .flatTap(t => Resource.liftF(Sync[F].delay(t.enterRawMode())))
      .map { underlying =>
        new Terminal[F] {
          def readChar: F[String] = {
            Sync[F]
              .delay(underlying.reader.read())
              .map(_.toChar.toString)
          }
          def putStrLn(s: String): F[Unit] = {
            Sync[F].delay {
              underlying.writer.println(s)
              underlying.writer.flush()
            }
          }

          def printTable(docs: List[MavenDoc], page: Int): F[Unit] = {
            Sync[F].delay {
              underlying.writer.println(buildTable(docs))
              underlying.writer.print(message(docs, page))
              underlying.writer.flush()
            }
          }
        }
      }
  }

  private def underlyingTerminalResource[F[_]: Sync]: Resource[F, terminal.Terminal] = {
    val acquire = Sync[F].delay(defaultBuilder.build())
    val release = (t: terminal.Terminal) => Sync[F].delay(t.close())

    Resource.make(acquire)(release)
  }

  private def defaultBuilder[F[_]: Sync] =
    TerminalBuilder
      .builder()
      .jna(true)
      .system(true)
      .dumb(true)
}

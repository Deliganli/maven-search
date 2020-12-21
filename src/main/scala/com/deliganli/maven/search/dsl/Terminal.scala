package com.deliganli.maven.search.dsl

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.deliganli.maven.search.Params.Config
import org.jline.terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.InfoCmp.Capability

trait Terminal[F[_]] {
  def readChar: F[String]
  def putStrLn(s: String): F[Unit]
  def printTable(table: String, page: Int, size: Int): F[Unit]
}

object Terminal {

  def apply[F[_]](implicit ev: Terminal[F]) = ev

  def sync[F[_]: Sync](config: Config): Resource[F, Terminal[F]] = {
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

          def printTable(table: String, page: Int, size: Int): F[Unit] = {
            Sync[F].delay {
              if (config.clearScreen) underlying.puts(Capability.clear_screen)
              underlying.writer.println(table)
              underlying.writer.print(message(size, page))
              if (!config.clearScreen) underlying.writer.println()
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

  def message(size: Int, page: Int): String = {
    val prev = if (page > 1) ", p:previous page" else ""
    s"""Page:$page
       |Select a number to copy to clipboard (1 - $size, n:next page$prev): """.stripMargin
  }
}

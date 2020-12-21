package com.deliganli.maven.search.dsl

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

import cats.effect.Sync
import cats.implicits._

trait Clipboard[F[_]] {
  def set(data: String): F[Unit]
}

object Clipboard {
  def apply[F[_]](implicit ev: Clipboard[F]) = ev

  def system[F[_]: Sync]: F[Clipboard[F]] =
    Sync[F]
      .delay(Toolkit.getDefaultToolkit.getSystemClipboard)
      .map { c =>
        new Clipboard[F] {
          override def set(data: String): F[Unit] =
            Sync[F].delay {
              val s = new StringSelection(data)
              c.setContents(s, s)
            }
        }
      }
}

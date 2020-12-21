package com.deliganli.maven.search.logging

import cats.effect.{Concurrent, ContextShift, Resource, Timer}
import io.odin.formatter.Formatter
import io.odin.{asyncFileLogger, Level, Logger}

object PresetLogger {

  def default[F[_]: Concurrent: Timer: ContextShift](level: Level = Level.Debug): Resource[F, Logger[F]] =
    asyncFileLogger[F]("maven-search.log", formatter = Formatter.colorful, minLevel = level)
}

package org.slf4j.impl

import cats.effect.{Clock, ContextShift, Effect, IO, Timer}
import com.deliganli.core.logging.PresetLogger
import io.odin._
import io.odin.slf4j.OdinLoggerBinder

import scala.concurrent.ExecutionContext

class ExternalLogger extends OdinLoggerBinder[IO] {

  implicit val F: Effect[IO]        = IO.ioEffect
  implicit val clock: Clock[IO]     = Clock.create
  implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.global)
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val (logger, _)                   = PresetLogger.default[IO]().allocated.unsafeRunSync()

  val loggers: PartialFunction[String, Logger[IO]] = {
    case _ => logger
  }
}

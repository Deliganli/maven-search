package com.deliganli.maven.search

import cats.data.Validated
import cats.implicits._
import com.deliganli.maven.search.Domain.Config
import com.deliganli.maven.search.Program.ProgramEvent.{Copy, Exit, Move, Search}
import com.deliganli.maven.search.Program.{ProgramEvent, State, UserEvent}

trait Transformer {
  def userToProgram(state: State)(e: UserEvent): ProgramEvent

  def stringToUserEvent(s: String): Validated[String, UserEvent]
}

object Transformer {

  def dsl[F[_]](config: Config): Transformer = {
    new Transformer {
      def userToProgram(state: State)(e: UserEvent): ProgramEvent = {
        def expected: PartialFunction[UserEvent, ProgramEvent] = {
          case UserEvent.Next if state.docs.size > state.page * config.itemPerPage => Move(state.page + 1)
          case UserEvent.Next if state.docs.size % config.itemPerPage == 0 => Search(state.page + 1)
          case UserEvent.Prev if state.page > 1 => Move(state.page - 1)
          case UserEvent.Selection(i)           => Copy(i)
        }

        expected.applyOrElse[UserEvent, ProgramEvent](e, _ => Exit)
      }

      def stringToUserEvent(s: String): Validated[String, UserEvent] = {
        s match {
          case s if s == "n" || s == "N" => UserEvent.Next.valid
          case s if s == "p" || s == "P" => UserEvent.Prev.valid
          case s                         => s.toIntOption.map(UserEvent.Selection).toValid(s)
        }
      }
    }
  }
}

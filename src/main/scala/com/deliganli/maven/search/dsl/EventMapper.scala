package com.deliganli.maven.search.dsl

import cats.data.Validated
import cats.implicits._
import com.deliganli.maven.search.Domain.{ProgramEvent, UserEvent}
import com.deliganli.maven.search.Params.Config

trait EventMapper {
  def userToProgram(size: Int, page: Int)(e: UserEvent): ProgramEvent

  def stringToUserEvent(s: String): Validated[String, UserEvent]
}

object EventMapper {

  def dsl(config: Config): EventMapper = {
    new EventMapper {
      def userToProgram(size: Int, page: Int)(e: UserEvent): ProgramEvent = {
        def expected: PartialFunction[UserEvent, ProgramEvent] = {
          case UserEvent.Next if size > page * config.itemPerPage => ProgramEvent.Move(page + 1)
          case UserEvent.Next if size % config.itemPerPage == 0 => ProgramEvent.Search(page + 1)
          case UserEvent.Prev if page > 1 => ProgramEvent.Move(page - 1)
          case UserEvent.Selection(i)     => ProgramEvent.Copy(i)
        }

        expected.applyOrElse[UserEvent, ProgramEvent](e, _ => ProgramEvent.Exit)
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

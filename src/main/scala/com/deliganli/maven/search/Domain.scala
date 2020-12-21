package com.deliganli.maven.search

import com.deliganli.maven.search.Domain.MavenModel.MavenDoc

object Domain {

  case class MavenModel(docs: List[MavenDoc])

  object MavenModel {

    case class MavenDoc(
      g: String,
      a: String,
      v: String,
      p: String)
  }

  sealed trait ImportFormat

  object ImportFormat {
    case object Sbt     extends ImportFormat
    //case object Maven   extends ImportFormat
    case object Generic extends ImportFormat
  }

  sealed trait ProgramEvent

  object ProgramEvent {
    case class Search(page: Int)    extends ProgramEvent
    case object Prompt              extends ProgramEvent
    case class Copy(selection: Int) extends ProgramEvent
    case class Move(page: Int)      extends ProgramEvent
    case object Exit                extends ProgramEvent
  }

  sealed trait UserEvent

  object UserEvent {
    case object Next             extends UserEvent
    case object Prev             extends UserEvent
    case class Selection(i: Int) extends UserEvent
  }

}

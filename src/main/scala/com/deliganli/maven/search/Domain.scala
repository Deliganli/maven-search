package com.deliganli.maven.search

import com.deliganli.maven.search.Domain.MavenModel.MavenDoc
import org.http4s.Uri

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
    class Sbt   extends ImportFormat
    class Maven extends ImportFormat
  }

  case class Config(
    mavenUri: Uri,
    chunkSize: Int,
    itemPerPage: Int,
    copyToClipboard: Boolean,
    importFormat: ImportFormat,
    debug: Boolean)

}

package com.deliganli.maven.search

import com.deliganli.maven.search.Domain.ImportFormat.{Generic, Sbt}
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc
import com.deliganli.maven.search.Domain.{ImportFormat, MavenModel}
import com.deliganli.maven.search.Params.Config
import io.circe.Decoder
import org.http4s.Uri

package object json {
  implicit val mavenDoc: Decoder[MavenDoc] = Decoder.forProduct4("g", "a", "latestVersion", "p")(MavenDoc.apply)

  implicit val mavenModel: Decoder[MavenModel] = Decoder.instance { c =>
    for {
      docs <- c.downField("response").downField("docs").as[List[MavenDoc]]
    } yield MavenModel(docs)
  }

  implicit val uri: Decoder[Uri] = Decoder.instance(_.as[String].map(Uri.unsafeFromString))

  implicit val importFormat: Decoder[ImportFormat] = Decoder.decodeString.emap {
    case "sbt"     => Right(Sbt)
    case "generic" => Right(Generic)
    case v         => Left(s"Unknown value: $v - valid values are sbt, maven")
  }

  implicit val config: Decoder[Config] = Decoder.forProduct7(
    "query",
    "mavenUri",
    "chunkSize",
    "itemPerPage",
    "importFormat",
    "debug",
    "clearScreen"
  )(Config.apply)
}

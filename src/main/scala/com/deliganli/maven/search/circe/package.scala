package com.deliganli.maven.search

import com.deliganli.maven.search.Domain.ImportFormat.{Maven, Sbt}
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc
import com.deliganli.maven.search.Domain.{Config, ImportFormat, MavenModel}
import io.circe.Decoder
import org.http4s.Uri

package object circe {
  implicit val mavenDoc: Decoder[MavenDoc] = Decoder.forProduct4("g", "a", "latestVersion", "p")(MavenDoc.apply)

  implicit val mavenModel: Decoder[MavenModel] = Decoder.instance { c =>
    for {
      docs <- c.downField("response").downField("docs").as[List[MavenDoc]]
    } yield MavenModel(docs)
  }

  implicit val uri: Decoder[Uri] = Decoder.instance(_.as[String].map(Uri.unsafeFromString))

  implicit val importFormat: Decoder[ImportFormat] = Decoder.decodeString.emap {
    case "sbt"   => Right(new Sbt())
    case "maven" => Right(new Maven())
    case v       => Left(s"Unknown value: $v - valid values are sbt, maven")
  }

  implicit val config: Decoder[Config] = Decoder.forProduct6(
    "mavenUri",
    "chunkSize",
    "itemPerPage",
    "copyToClipboard",
    "importFormat",
    "debug"
  )(Config.apply)
}

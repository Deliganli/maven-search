package com.deliganli.maven.search

import com.deliganli.maven.search.Domain.ImportFormat.Sbt
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc

import scala.util.matching.Regex

trait Formatter[T] {
  def format(doc: MavenDoc): String
}

object Formatter {
  def apply[T](implicit ev: Formatter[T]) = ev

  implicit val sbt: Formatter[Sbt] = new Formatter[Sbt] {
    val scalaVersioned: Regex   = """(.*)_\d*.\d*""".r
    val scalaJsVersioned: Regex = """(.*)_sjs\d*.\d*""".r

    override def format(doc: MavenDoc): String = {
      val artifact = None
        .orElse(scalaJsVersioned.findFirstMatchIn(doc.a).map(m => s"""%%% "${m.group(1)}""""))
        .orElse(scalaVersioned.findFirstMatchIn(doc.a).map(m => s"""%% "${m.group(1)}""""))
        .getOrElse(s"""% "${doc.a}"""")

      raw""""${doc.g}" ${artifact} % "${doc.v}""""
    }
  }
}

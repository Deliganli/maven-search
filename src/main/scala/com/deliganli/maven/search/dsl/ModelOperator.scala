package com.deliganli.maven.search.dsl

import cats.implicits._
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc

trait ModelOperator[A] {
  def mavenToModel(docs: List[MavenDoc]): List[A]
  def modelToTable(items: List[A]): String
  def format(item: A): String
}

object ModelOperator {

  case class ScalaGrouped(
    group: String,
    artifact: String,
    version: String,
    sjs: Boolean,
    scala: Boolean)

  val sbt: ModelOperator[ScalaGrouped] =
    new ModelOperator[ScalaGrouped] {
      val sbtRegex = """((?<scalav>\d*\.\d*)_)?((?<sjsv>\d*\.\d*)sjs_)?(?<artifact>.*)""".r

      def refineArtifact(doc: MavenDoc): (String, Option[String], Option[String]) = {
        sbtRegex
          .findFirstMatchIn(doc.a.reverse)
          .map(r => (r.group("artifact").reverse, Option(r.group("scalav")).map(_.reverse), Option(r.group("sjsv")).map(_.reverse)))
          .getOrElse(("", None, None))
      }

      override def mavenToModel(docs: List[MavenDoc]): List[ScalaGrouped] = {
        docs
          .fproduct(refineArtifact)
          .groupMap { case (doc, (a, sc, sj)) => (doc.g, a, doc.v) } { case (doc, (a, sc, sj)) => (sc, sj) }
          .toList
          .flatMap {
            case ((g, a, v), vs) =>
              if (vs.exists(_._2.nonEmpty)) {
                List(
                  ScalaGrouped(g, a, v, vs.exists(_._1.nonEmpty), vs.exists(_._1.nonEmpty)),
                  ScalaGrouped(g, a, v, sjs = false, scala = vs.exists(_._1.nonEmpty))
                )
              } else {
                List(ScalaGrouped(g, a, v, sjs = false, scala = vs.exists(_._1.nonEmpty)))
              }
          }
      }

      override def modelToTable(items: List[ScalaGrouped]): String = {
        val (gSize, aSize, vSize) = maxSizes(items.map(x => (x.group, x.artifact, x.version)))

        items.zipWithIndex
          .map {
            case (d, i) =>
              val id = "[" + (i + 1) + "]"
              val template =
                if (d.sjs) s"%4s %${gSize}s %%%%%% %${aSize}s %% %${vSize}s"
                else if (d.scala) s"%4s %${gSize}s %%%%  %${aSize}s %% %${vSize}s"
                else s"%4s %${gSize}s %%    %${aSize}s %% %${vSize}s"

              String.format(template, id, d.group, d.artifact, d.version)
          }
          .mkString("\n")
      }

      override def format(item: ScalaGrouped): String = {
        val sep = if (item.sjs) "%%%" else if (item.scala) "%%" else "%"
        raw""""${item.group}" $sep "${item.artifact}" % "${item.version}""""
      }
    }

  case class Generic(group: String, artifact: String, version: String)

  val generic: ModelOperator[Generic] = new ModelOperator[Generic] {
    override def mavenToModel(docs: List[MavenDoc]): List[Generic] = docs.map(d => Generic(d.g, d.a, d.v))

    override def modelToTable(items: List[Generic]): String = {
      val (gSize, aSize, vSize) = maxSizes(items.map(x => (x.group, x.artifact, x.version)))

      val template = s"%4s %${gSize}s : %${aSize}s : %${vSize}s"

      items.zipWithIndex
        .map { case (d, i) => String.format(template, "[" + (i + 1) + "]", d.group, d.artifact, d.version) }
        .mkString("\n")
    }

    override def format(item: Generic): String = {
      raw""""${item.group}" : "${item.artifact}" : "${item.version}""""
    }
  }

  def maxSizes(xs: List[(String, String, String)]) = {
    xs.foldLeft(0, 0, 0) { case ((g, a, v), (dg, da, dv)) => (dg.length.max(g), da.length.max(a), dv.length.max(v)) }
  }
}

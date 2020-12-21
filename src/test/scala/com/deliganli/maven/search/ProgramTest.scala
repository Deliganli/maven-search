package com.deliganli.maven.search

import cats.effect.IO
import cats.implicits._
import com.deliganli.maven.search.Domain.MavenModel
import com.deliganli.maven.search.Domain.MavenModel.MavenDoc
import com.deliganli.maven.search.Tabulate.TabulateSyntax
import com.deliganli.maven.search.json._
import com.deliganli.maven.search.dsl.ModelOperator
import io.circe.parser._

class ProgramTest extends UnitTest {

  "Program" should "tabulate" in {
    UnitTest
      .resource("applicative.json")
      .use(c =>
        parse(c)
          .flatMap(_.as[MavenModel].map(_.docs))
          .map(ModelOperator.generic.mavenToModel)
          .map(ModelOperator.generic.modelToTable)
          .pure[IO]
      )
      .unsafeRunSync() shouldBe Right(
      t""" [1] xyz.funjava.functional :         higherkinded : 0.0.1
         | [2]         com.propensive : mercator_sjs0.6_2.13 : 0.3.0
         | [3]         com.propensive :        mercator_2.13 : 0.3.0
         | [4]         com.propensive : mercator_sjs0.6_2.12 : 0.3.0
         | [5]         com.propensive :        mercator_2.12 : 0.3.0
         | [6]           io.javaslang :       javaslang-pure : 2.0.6
         | [7]           io.javaslang :       javaslang-pure : 2.0.5
         | [8]           io.javaslang :       javaslang-pure : 2.0.4
         | [9]           io.javaslang :       javaslang-pure : 2.0.3
         |[10]           io.javaslang :       javaslang-pure : 2.0.2"""
    )
  }

  "Formatter" should "format for sbt" in {
    val doc       = MavenDoc("com.propensive", "mercator_sjs0.6_2.13", "0.3.0", "jar")
    val P         = ModelOperator.sbt
    val items     = P.mavenToModel(List(doc))
    val formatted = P.format(items.head)

    formatted shouldBe raw""""com.propensive" %%% "mercator" % "0.3.0""""
  }

  "Tabulate" should "group correctly for sbt" in {
    val raw = List(
      MavenDoc("com.deliganli", "maven-search", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven-search_2.12", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven-search_2.13", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven-search_sjs0.6_2.12", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven-search_sjs0.6_2.13", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven_search", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven_search_2.12", "0.1.0", "?"),
      MavenDoc("com.deliganli", "maven_search_sjs1.0_2.13", "0.1.0", "?")
    )

    val P     = ModelOperator.sbt
    val items = P.mavenToModel(raw)
    val table = P.modelToTable(items)

    table shouldBe
      t""" [1] com.deliganli %   maven-search % 0.1.0
         | [2] com.deliganli %%  maven-search % 0.1.0
         | [3] com.deliganli %%% maven-search % 0.1.0
         | [4] com.deliganli %   maven_search % 0.1.0
         | [5] com.deliganli %%  maven_search % 0.1.0
         | [6] com.deliganli %%% maven_search % 0.1.0"""
  }
}

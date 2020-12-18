package com.deliganli.maven.search

import com.deliganli.maven.search.Domain.MavenModel.MavenDoc

object Visual {

  def buildTable(docs: List[MavenDoc]): String = {
    val (gSize, aSize, vSize) = docs.foldLeft(0, 0, 0) {
      case ((g, a, v), d) => (d.g.length.max(g), d.a.length.max(a), d.v.length.max(v))
    }

    docs.zipWithIndex
      .map { case (d, i) => String.format(s"%4s %${gSize}s : %${aSize}s : %${vSize}s", "[" + (i + 1) + "]", d.g, d.a, d.v) }
      .mkString("\n")
  }

  def message(docs: List[MavenDoc], page: Int): String = {
    val prev = if (page > 1) ", p:previous page" else ""
    s"""Page:$page
       |Select a number to copy to clipboard (1 - ${docs.size}, n:next page${prev}): """.stripMargin
  }

}

package com.deliganli.maven.search

object Tabulate {

  implicit class TabulateSyntax(val sc: StringContext) extends AnyVal {

    def t(args: Any*): String = {
      val strings     = sc.parts.iterator
      val expressions = args.iterator
      var buf         = new StringBuilder(strings.next())
      while (strings.hasNext) {
        buf.append(expressions.next())
        buf.append(strings.next())
      }

      raw"$buf".stripMargin.replace("\r\n", "\n")
    }
  }
}

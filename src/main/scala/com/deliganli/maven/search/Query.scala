package com.deliganli.maven.search

import org.http4s.Uri

object Query {

  def search(
    uri: Uri,
    query: String,
    start: Int,
    rows: Int
  ) =
    uri
      .withQueryParam("start", start)
      .withQueryParam("rows", rows)
      .withQueryParam("q", query)

  def versions(uri: Uri, g: String, a: String) =
    uri
      .withQueryParam("rows", 9)
      .withQueryParam("q", s"g:$g AND a:$a")
      .withQueryParam("core", "gav")
}

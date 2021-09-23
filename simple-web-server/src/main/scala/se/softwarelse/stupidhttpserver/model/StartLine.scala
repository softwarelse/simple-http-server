package se.softwarelse.stupidhttpserver.model

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

case class StartLine(method: String, uri: String, httpVersion: String) {
  val params: Seq[KV] = uri.split('?').drop(1).headOption.getOrElse("").split('&').map(parseParam).filter(_.nonEmpty)
  val path: String = uri.split('?').head

  private def parseParam(in: String): KV = {
    val parts = in.split('=')
    val key = URLDecoder.decode(parts.headOption.getOrElse("").trim, StandardCharsets.UTF_8)
    val value = URLDecoder.decode(parts.drop(1).headOption.getOrElse("").trim, StandardCharsets.UTF_8)
    KV(key, value)
  }
}

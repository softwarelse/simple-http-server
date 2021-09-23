package se.softwarelse.stupidhttpserver.model

import java.net.URL
import java.nio.charset.StandardCharsets
import scala.util.Try

case class Request(startLine: StartLine, headers: Seq[KV], body: Option[Array[Byte]]) {
  lazy val method: String = startLine.method
  lazy val uri: String = startLine.uri
  lazy val params: Seq[KV] = startLine.params
  lazy val host: Option[String] = findHeaderValue("host").map(_.split(':').head)
  lazy val contentLength: Int = findHeaderValue("Content-Length").map(_.toInt).getOrElse(0)
  lazy val TransferEncodingChunked: Boolean = findHeaderValue("Transfer-Encoding").map(_.toLowerCase.trim).contains("chunked")
  lazy val bodyAsUtf8: Option[String] = body.flatMap(bs => Try(new String(bs, StandardCharsets.UTF_8)).toOption)
  lazy val path: String = startLine.path

  require(!TransferEncodingChunked, "Transfer-Encoding: Chunked is not supported!")

  def findHeader(key: String): Option[KV] = {
    headers.find(_.key.trim.toLowerCase == key.trim.toLowerCase)
  }

  def findHeaderValue(key: String): Option[String] = {
    findHeader(key).map(_.value)
  }

  override def toString: String = {
    s"$method $path { host=$host, params=$params, body=$bodyAsUtf8, headers=$headers }"
  }
}

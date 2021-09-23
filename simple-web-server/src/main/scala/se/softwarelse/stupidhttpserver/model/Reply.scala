package se.softwarelse.stupidhttpserver.model

import se.softwarelse.stupidhttpserver.httpEndl

import java.nio.charset.StandardCharsets

case class Reply(status: Int, version: String, customHeaders: Seq[KV], body: Option[Array[Byte]]) {
  val defaultBody: Array[Byte] = Array[Byte]()
  val bodyBytes: Array[Byte] = body.getOrElse(defaultBody)
  val bodyBytesLen: Int = bodyBytes.length
  val defaultHeaders: Seq[KV] = Seq(
    KV("Server", "StupidHttpServer"),
    KV("Content-Length", s"$bodyBytesLen"),
    KV("Connection", "Closed"),
  )
  val allHeaders: Seq[KV] = defaultHeaders ++ customHeaders
  lazy val httpResponseStringHeaders: String = allHeaders.map(h => s"${h.key}: ${h.value}").mkString("", httpEndl, httpEndl)
  lazy val httpResponseStringHead: String =
    version + " " + status + httpEndl +
      httpResponseStringHeaders + httpEndl
}

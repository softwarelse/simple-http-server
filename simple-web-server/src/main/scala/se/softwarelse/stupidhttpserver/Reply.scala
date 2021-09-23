package se.softwarelse.stupidhttpserver

import java.nio.charset.StandardCharsets

case class Reply(status: Int, version: String, customHeaders: Seq[KV], body: Option[String]) {
  val defaultBody = ""
  val bodyBytes: Array[Byte] = body.getOrElse(defaultBody).getBytes(StandardCharsets.UTF_8)
  val bodyBytesLen: Int = bodyBytes.length
  val defaultHeaders: Seq[KV] = Seq(
    KV("Server", "StupidHttpServer"),
    KV("Content-Length", s"$bodyBytesLen"),
    KV("Connection", "Closed"),
  )
  val allHeaders: Seq[KV] = defaultHeaders ++ customHeaders
  lazy val httpResponseStringHeaders: String = allHeaders.map(h => s"${h.key}: ${h.value}").mkString("", httpEndl, httpEndl)
  lazy val httpResponseString: String =
    version + " " + status + httpEndl +
      httpResponseStringHeaders + httpEndl +
      body.getOrElse(defaultBody)
}
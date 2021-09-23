package se.softwarelse.stupidhttpserver.workers

import se.softwarelse.stupidhttpserver.model.{KV, Request, StartLine}
import se.softwarelse.stupidhttpserver.{httpEndl, lf}

import java.io.{BufferedReader, ByteArrayOutputStream, InputStream, StringReader}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

object RequestParser {

  def parse(stream: InputStream): Request = {
      val rawHeadString: String = readHead(stream)
      val headReader = new BufferedReader(new StringReader(rawHeadString))
      val startLine: StartLine = readStartLine(headReader)
      val headers: Seq[KV] = readHeaders(headReader)
      val requestWoBody = Request(startLine = startLine, headers = headers, body = None)
      val bodyBytes = stream.readNBytes(requestWoBody.contentLength)
      requestWoBody.copy(body = Some(bodyBytes).filter(_.nonEmpty))
  }

  private def readHead(stream: InputStream): String = {

    val byteBuffer = new ByteArrayOutputStream
    val charBuffer = new StringBuilder

    val possibleSectionsEnds: Seq[String] = Seq(
      httpEndl + httpEndl,
      lf + httpEndl,
      httpEndl + lf,
      lf + lf,
    )

    def bufferEndsWithDoubleEndl: Boolean = {
      possibleSectionsEnds.exists(end => charBuffer.endsWith(end))
    }

    var eof = false
    while (!bufferEndsWithDoubleEndl && !eof) {
      val byteVal: Int = stream.read()
      eof = byteVal < 0
      if (!eof) {
        charBuffer += byteVal.toChar
        byteBuffer.write(byteVal)
      }
    }

    new String(byteBuffer.toByteArray, StandardCharsets.UTF_8)
  }

  private def readStartLine(reader: BufferedReader): StartLine = {
    val rawLine = reader.readLine()
    val parts = rawLine.split(' ')
    StartLine(parts(0), parts(1), parts(2))
  }

  @tailrec
  private def readHeaders(reader: BufferedReader, acc: Seq[KV] = Nil): Seq[KV] = {
    val nextHeaderLineRaw: String = reader.readLine()
    if (nextHeaderLineRaw.isEmpty) {
      acc
    }
    else {
      val splitterIndex: Int = nextHeaderLineRaw.indexOf(':')
      val (key, value) = if (splitterIndex >= 0) {
        val (k, v) = nextHeaderLineRaw.splitAt(splitterIndex)
        (k.trim, v.drop(1).trim)
      }
      else {
        (nextHeaderLineRaw, "")
      }
      readHeaders(reader, acc :+ KV(key, value))
    }
  }
}

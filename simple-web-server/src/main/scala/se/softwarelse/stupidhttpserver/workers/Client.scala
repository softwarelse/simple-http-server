package se.softwarelse.stupidhttpserver.workers

import se.softwarelse.stupidhttpserver._
import se.softwarelse.stupidhttpserver.model.{KV, Reply, Request, StartLine}

import java.io.{BufferedInputStream, BufferedReader, ByteArrayOutputStream, StringReader}
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import scala.annotation.tailrec
import scala.util.Try
import scala.util.control.NonFatal

class Client(clientSocket: Socket) extends Runnable {

  private val log: Logger = Logger.getLogger(getClass.getName)

  override def run(): Unit = {
    try {
      val stream = new BufferedInputStream(clientSocket.getInputStream)
      while (!clientSocket.isClosed && !clientSocket.isInputShutdown) {
        val rawHeadString: String = readHead(stream)
        val headReader = new BufferedReader(new StringReader(rawHeadString))
        val startLine: StartLine = readStartLine(headReader)
        val headers: Seq[KV] = readHeaders(headReader)
        val requestWoBody = Request(startLine, headers, None)
        val bodyBytes = stream.readNBytes(requestWoBody.contentLength)
        val request = requestWoBody.copy(body = Some(bodyBytes).filter(_.nonEmpty))
        val reply = process(request)
        sendReply(reply)
        clientSocket.close()
      }
    } catch {
      case NonFatal(e) =>
        log.severe(s"Shit went wrong with client socket $clientSocket due to $e, shutting down")
        if (!clientSocket.isOutputShutdown && !clientSocket.isClosed) {
          Try(sendReply(Reply(
            status = 500,
            version = "HTTP/1.1",
            customHeaders = Nil,
            body = Some(s"Shit went wrong. Check server logs for details") // intentionally not leaking internal errors
          )))
        }
        Try(clientSocket.close()) // just eat any errors from .close()
    }
    Try(clientSocket.close()) // just eat any errors from .close()
  }

  private def readHead(stream: BufferedInputStream): String = {

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

  private def sendReply(reply: Reply): Unit = {
    clientSocket.getOutputStream.write(reply.httpResponseString.getBytes(StandardCharsets.UTF_8))
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
      val headerParts = nextHeaderLineRaw.split(':').map(_.trim)
      readHeaders(reader, acc :+ KV(headerParts(0), headerParts(1))) // yeye, we will fail on invalid input :)
    }
  }

  private def process(request: Request): Reply = {
    log.info(s"Processing shit: $request")
    val paramsJson: String = request.params.map(_.toJson).mkString(",")
    val headersJson: String = request.headers.map(_.toJson).mkString(",")
    // TODO: Do something here instead..
    Reply(
      status = 200,
      version = "HTTP/1.1",
      customHeaders = Seq(KV("Content-Type", "application/json")),
      body = Some(s"""{ "your-params": [ $paramsJson ], "your-headers": [ $headersJson ] }""")
    )
  }

}
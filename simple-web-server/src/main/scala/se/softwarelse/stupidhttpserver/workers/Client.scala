package se.softwarelse.stupidhttpserver.workers

import com.invidi.simplewebserver.context.WebServerContext
import se.softwarelse.stupidhttpserver._
import se.softwarelse.stupidhttpserver.model.{KV, Reply, Request, StartLine}

import java.io.{BufferedInputStream, BufferedReader, ByteArrayOutputStream, StringReader}
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import scala.annotation.tailrec
import scala.util.Try
import scala.util.control.NonFatal

class Client(clientSocket: Socket,
             webServerContext: WebServerContext) extends Runnable {

  private val log: Logger = Logger.getLogger(getClass.getName)

  override def run(): Unit = {
    try {
      val stream = new BufferedInputStream(clientSocket.getInputStream)
      while (!clientSocket.isClosed && !clientSocket.isInputShutdown) {
        val rawHeadString: String = readHead(stream)
        val headReader = new BufferedReader(new StringReader(rawHeadString))
        val startLine: StartLine = readStartLine(headReader)
        val headers: Seq[KV] = readHeaders(headReader)
        val requestWoBody = Request(startLine = startLine, headers = headers, body = None)
        val bodyBytes = stream.readNBytes(requestWoBody.contentLength)
        val request = requestWoBody.copy(body = Some(bodyBytes).filter(_.nonEmpty))
        val reply = Processor.process(request, webServerContext)
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
            body = Some(s"Shit went wrong. Check server logs for details".getBytes(StandardCharsets.UTF_8)) // intentionally not leaking internal errors
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
    clientSocket.getOutputStream.write(reply.httpResponseStringHead.getBytes(StandardCharsets.UTF_8))
    reply.body.foreach(array => clientSocket.getOutputStream.write(array))
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
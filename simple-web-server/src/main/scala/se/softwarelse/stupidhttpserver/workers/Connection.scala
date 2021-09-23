package se.softwarelse.stupidhttpserver.workers

import com.invidi.simplewebserver.context.WebServerContext
import se.softwarelse.stupidhttpserver.model.{HttpServiceException, Reply}

import java.io.BufferedInputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import scala.util.Try
import scala.util.control.NonFatal

class Connection(clientSocket: Socket,
                 webServerContext: WebServerContext) extends Runnable {

  private val log: Logger = Logger.getLogger(getClass.getName)

  override def run(): Unit = {
    try {
      val stream = new BufferedInputStream(clientSocket.getInputStream)
      while (!clientSocket.isClosed && !clientSocket.isInputShutdown) {
        val request = RequestParser.parse(stream)
        val reply = Processor.process(request, webServerContext)
        sendReply(reply)
      }
    } catch {
      case HttpServiceException(status, msg) =>
        log.warning(s"Shit went wrong with the request, status: $status, msg: $msg")
        sendReplyIgnoreErrors(status, msg)
      case NonFatal(e) =>
        log.severe(s"Shit went wrong with client socket $clientSocket due to $e, shutting down")
        sendReplyIgnoreErrors(500, s"Shit went wrong. Check server logs for details")
    }
    Try(clientSocket.close()) // just eat any errors from .close()
  }

  private def sendReplyIgnoreErrors(code: Int, msg: String): Unit = Try {
    if (!clientSocket.isOutputShutdown && !clientSocket.isClosed) {
      sendReply(Reply(
        status = code,
        version = "HTTP/1.1",
        customHeaders = Nil,
        body = Some(msg.getBytes(StandardCharsets.UTF_8))
      ))
    }
  }

  private def sendReply(reply: Reply): Unit = {
    clientSocket.getOutputStream.write(reply.httpResponseStringHead.getBytes(StandardCharsets.UTF_8))
    reply.body.foreach(array => clientSocket.getOutputStream.write(array))
  }

}
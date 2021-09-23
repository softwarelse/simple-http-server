package se.softwarelse.stupidhttpserver.workers

import com.invidi.simplewebserver.context.WebServerContext

import java.net.ServerSocket
import java.util.logging.Logger
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class Accepter(serverSocket: ServerSocket,
               webServerContext: WebServerContext) extends Runnable {

  private val log: Logger = Logger.getLogger(getClass.getName)

  override def run(): Unit = {
    while (!serverSocket.isClosed) {
      try {
        val clientSocket = serverSocket.accept()
        ExecutionContext.global.execute(new Client(clientSocket, webServerContext))
      } catch {
        case NonFatal(e) =>
          log.severe(s"Shit went wront with socket.accept: $e") // stupid java logger cant show me stack traces
      }
    }
  }
}
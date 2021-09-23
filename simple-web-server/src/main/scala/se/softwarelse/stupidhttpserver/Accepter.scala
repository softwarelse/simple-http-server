package se.softwarelse.stupidhttpserver

import java.net.ServerSocket
import java.util.logging.Logger
import scala.util.control.NonFatal

class Accepter(serverSocket: ServerSocket) extends Runnable {

  private val log: Logger = Logger.getLogger(getClass.getName)

  override def run(): Unit = {
    while (!serverSocket.isClosed) {
      try {
        val clientSocket = serverSocket.accept()
        new Thread(new Client(clientSocket)).start() // Better to use a limited thread pool here ;)
      } catch {
        case NonFatal(e) =>
          log.severe(s"Shit went wront with socket.accept: $e") // stupid java logger cant show me stack traces
      }
    }
  }
}
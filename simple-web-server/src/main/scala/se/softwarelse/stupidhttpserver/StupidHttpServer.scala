package se.softwarelse.stupidhttpserver

import com.invidi.simplewebserver.context.WebServerContext
import com.invidi.simplewebserver.main.WebServer
import se.softwarelse.stupidhttpserver.workers.Accepter

import java.net.ServerSocket
import java.util.logging.Logger

class StupidHttpServer extends WebServer {

  private val log: Logger = Logger.getLogger(getClass.getName)
  private var serverSocket: ServerSocket = _

  override def start(port: Int): Unit = {
    require(serverSocket == null, s"Dont start twice!")
    log.info(s"Starting $this on port $port")
    serverSocket = new ServerSocket(port)
    new Thread(new Accepter(serverSocket)).start()
  }

  override def stop(): Unit = {
    require(serverSocket != null, s"Cant stop not yet started server!")
    serverSocket.close()
  }

  override def getWebContext: WebServerContext = {
    // Wtf do I need this for :D
    ???
  }

}

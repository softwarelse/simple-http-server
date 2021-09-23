package se.softwarelse.stupidhttpserver

import com.invidi.simplewebserver.context.WebServerContext

import java.util.UUID

class StupidHttpServerContext extends WebServerContext {

  @volatile var staticPath: String = "/" + UUID.randomUUID().toString

  override def setStaticPath(path: String): Unit = this.staticPath = path

  override def getStaticPath(): String = staticPath
}

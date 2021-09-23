package se.softwarelse.stupidhttpserver.workers

import com.invidi.simplewebserver.context.WebServerContext
import se.softwarelse.stupidhttpserver.model.{KV, Reply, Request, ResponseData}

import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.logging.Logger

object Processor {

  private val log: Logger = Logger.getLogger(getClass.getName)

  def process(request: Request, context: WebServerContext): Reply = {
    log.info(s"Processing request: $request")

    val responseDataOpt: Option[ResponseData] =
      processStatic(request, context)
        .orElse(processDynamic(request, context))

    responseDataOpt match {
      case None =>
        Reply(
          status = 404,
          version = "HTTP/1.1",
          customHeaders = Nil,
          body = Some(s"Shit doesnt exist".getBytes(StandardCharsets.UTF_8))
        )
      case Some(data) =>
        Reply(
          status = 200,
          version = "HTTP/1.1",
          customHeaders = Seq(KV("Content-Type", data.contentType)),
          body = Some(data.data)
        )
    }
  }

  private def processStatic(request: Request, context: WebServerContext): Option[ResponseData] = {
    val resourcePath: String = if (request.path.isEmpty || request.path == "/") {
      "static/index.html"
    }
    else {
      s"static/${request.path}"
    }
    val fileStream: InputStream = getClass.getClassLoader.getResourceAsStream(resourcePath)
    if (fileStream != null) {
      Some(ResponseData.deduceType(
        path = resourcePath,
        data = fileStream.readAllBytes()
      ))
    }
    else {
      None
    }
  }

  private def processDynamic(request: Request, context: WebServerContext): Option[ResponseData] = {
    Option(context.getHandler(request.method, request.path)) map { handler =>
      Option(handler.invoke(request)) match {
        case None => ResponseData(Array[Byte](), "text/raw")
        case Some(data) => ResponseData(data.getBytes(StandardCharsets.UTF_8), "application/json")
      }
    }
  }
}



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

    val responseDataOpt: Option[ResponseData] = if (request.effectivePath.startsWith(context.getStaticPath)) {
      processStatic(request, context)
    }
    else {
      processDynamic(request, context)
    }

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
    val subPath = request.effectivePath.drop(context.getStaticPath.length)
    val resourcePath: String = if (subPath.isEmpty || subPath == "/") {
      "static/index.html"
    }
    else {
      s"static/$subPath"
    }
    val fileStream: InputStream = if (subPath.isEmpty || subPath == "/") {
      getClass.getClassLoader.getResourceAsStream(resourcePath)
    }
    else {
      getClass.getClassLoader.getResourceAsStream(resourcePath)
    }
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
    // TODO: Do something here instead.. Find a controller
    val paramsJson: String = request.params.map(_.toJson).mkString(",")
    val headersJson: String = request.headers.map(_.toJson).mkString(",")
    Some(ResponseData.json(s"""{ "your-params": [ $paramsJson ], "your-headers": [ $headersJson ] }"""))
  }
}



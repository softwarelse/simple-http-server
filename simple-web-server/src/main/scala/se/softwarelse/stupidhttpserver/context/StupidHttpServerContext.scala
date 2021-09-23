package se.softwarelse.stupidhttpserver.context

import com.invidi.simplewebserver.annotations.{Path, PathParam, QueryParam, RestController}
import com.invidi.simplewebserver.context.{RequestHandler, WebServerContext}
import se.softwarelse.stupidhttpserver.model.{HttpServiceException, Request}

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.util.UUID
import java.util.logging.Logger
import scala.collection.concurrent.TrieMap

class StupidHttpServerContext extends WebServerContext {

  override def setStaticPath(path: String): Unit = this.staticPath = path

  override def getStaticPath(): String = staticPath

  override def getHandler(method: String, requestPath: String): RequestHandler = {

    // TODO: Pre-compute these monsters..

    val controllerMappingOpt: Option[ControllerMapping] = mappings.keySet
      .find(prefix => requestPath.startsWith(prefix))
      .flatMap(mappings.get)

    val methodMappingOpt: Option[MethodMapping] =
      controllerMappingOpt.flatMap(_.getMethodMapping(method, requestPath))

    (for {
      controllerMapping <- controllerMappingOpt
      methodMapping <- methodMappingOpt
    } yield {

      if (methodMapping.paramMappings.count(_.isPathParam) > 0) {
        throw new RuntimeException(s"path parameters are not supported (yet?)!")
      }

      if (methodMapping.requiredHeaders.nonEmpty) {
        throw new RuntimeException(s"required headers are not supported (yet?)!")
      }

      new RequestHandler {

        override def invoke(request: Request): Object = {

          // Currently we just support strings in query params
          val args: Seq[Object] = methodMapping.paramMappings.map { paramMapping =>
            request
              .findQueryParamValue(paramMapping.key)
              .getOrElse(throw HttpServiceException(400, s"Missing query parameter: ${paramMapping.key}"))
          }

          methodMapping.classMethod
            .invoke(controllerMapping.controller, args: _*)

        }
      }
    }).orNull
  }

  override def addController(controller: Object): Unit = {

    val classAnnotations: Seq[Annotation] = controller.getClass.getAnnotations

    val restControllerAnnotation: RestController =
      classAnnotations.require(classOf[RestController])

    log.info(s"Registering ${classOf[RestController].getSimpleName}: $controller")
    val pathPrefix = restControllerAnnotation.value()

    val allMethods: Seq[Method] = controller.getClass.getMethods
    val methodsWithPathAnnotation: Seq[(Method, Path)] = allMethods.map(m => m -> Option(m.getAnnotation(classOf[Path]))).collect {
      case (m, Some(pathAnnotation)) =>
        m -> pathAnnotation
    }

    val methodMappings: Seq[MethodMapping] = for ((method, pathAnnotation) <- methodsWithPathAnnotation) yield {

      val paramMappings: Seq[ParameterMapping] = method.getParameters.map { methodParam =>
        Option(methodParam.getAnnotation(classOf[QueryParam])).map { queryParamAnnotation =>
          ParameterMapping(
            key = queryParamAnnotation.value(),
            requiredInHttpRequest = queryParamAnnotation.required(),
            default = if (queryParamAnnotation.required()) None else Option(queryParamAnnotation.defaultValue()),
            source = Left(queryParamAnnotation),
          )
        }.orElse(Option(methodParam.getAnnotation(classOf[PathParam])).map { pathParamAnnotation =>
          throw new RuntimeException(s"Path Parameters are not yet implemented!")
          ParameterMapping(
            key = pathParamAnnotation.value(),
            requiredInHttpRequest = true,
            default = None,
            source = Right(pathParamAnnotation),
          )
        }).getOrElse(throw new IllegalArgumentException(s"method $method parameter $methodParam lacks http parameter annotation"))
      }

      log.info(s"Mapping ${pathAnnotation.method()} $pathPrefix${pathAnnotation.value()} -> ${controller.getClass.getSimpleName}.${method.getName}")

      MethodMapping(
        httpMethod = pathAnnotation.method().toString,
        subPath = pathAnnotation.value(), // Need to make regex using path-params
        classMethod = method,
        paramMappings = paramMappings,
        requiredHeaders = pathAnnotation.headers(),
      )

    }

    mappings.put(pathPrefix, ControllerMapping(
      pathPrefix = pathPrefix,
      controller = controller,
      methodMappings = methodMappings,
    ))
  }

  private val log: Logger = Logger.getLogger(getClass.getName)
  private val mappings = new TrieMap[String, ControllerMapping]()
  @volatile var staticPath: String = "/" + UUID.randomUUID().toString

}



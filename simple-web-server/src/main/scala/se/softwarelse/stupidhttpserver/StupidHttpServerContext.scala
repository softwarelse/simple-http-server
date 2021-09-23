package se.softwarelse.stupidhttpserver

import com.invidi.simplewebserver.annotations.{Path, RestController}
import com.invidi.simplewebserver.context.WebServerContext

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.util.UUID
import java.util.logging.Logger
import scala.collection.concurrent.TrieMap

class StupidHttpServerContext extends WebServerContext {

  private val log: Logger = Logger.getLogger(getClass.getName)

  private val mappings = new TrieMap[String, ControllerMapping]()

  @volatile var staticPath: String = "/" + UUID.randomUUID().toString

  override def setStaticPath(path: String): Unit = this.staticPath = path

  override def getStaticPath(): String = staticPath

  override def addController(controller: Object): Unit = {

    val classAnnotations: Seq[Annotation] = controller.getClass.getAnnotations.toSeq

    val restControllerAnnotation: RestController =
      classAnnotations.require(classOf[RestController])

    log.info(s"Registering ${classOf[RestController].getSimpleName}: $controller")
    val pathPrefix = restControllerAnnotation.value()

    val allMethods = controller.getClass.getMethods.toSeq
    val methodsWithPathAnnotation: Seq[(Method, Path)] = allMethods.map(m => m -> Option(m.getAnnotation(classOf[Path]))).collect {
      case (m, Some(pathAnnotation)) =>
        m -> pathAnnotation
    }

    for ((method, pathAnnotation) <- methodsWithPathAnnotation) {
      log.info(s"Mapping $method using $pathAnnotation")
      //mappings.put(pathPrefix, controller)
    }
  }

  case class ControllerMapping(httpMethod: String,
                               pathPrefix: String,
                               subPath: String,
                               controller: Object,
                               classMethod: Method)

  private implicit class findAnnotationInList(list: Seq[Annotation]) {

    def find[T](cls: Class[_ <: T]): Option[T] = {
      list
        .find(_.annotationType() == cls)
        .map(_.asInstanceOf[T])
    }

    def require[T](cls: Class[_ <: T]): T = {
      find(cls).getOrElse(throw new IllegalArgumentException(s"could not find $cls Annotation"))
    }
  }
}



package se.softwarelse.stupidhttpserver.context

case class ControllerMapping(pathPrefix: String,
                             controller: Object,
                             methodMappings: Seq[MethodMapping]
                            ) {
  def getMethodMapping(method: String, requestFullPath: String): Option[MethodMapping] = {
    val requestSubPath: String = requestFullPath.drop(pathPrefix.length)
    methodMappings.find(m => m.httpMethod == method && m.subPath == requestSubPath)
  }
}

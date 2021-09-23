package se.softwarelse.stupidhttpserver.context

import java.lang.reflect.Method

case class MethodMapping(httpMethod: String,
                         subPath: String,
                         classMethod: Method,
                         paramMappings: Seq[ParameterMapping],
                         requiredHeaders: Seq[String],
                        ) {

}

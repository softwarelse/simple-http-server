package se.softwarelse.stupidhttpserver.context

import com.invidi.simplewebserver.annotations.{PathParam, QueryParam}

case class ParameterMapping(key: String,
                            requiredInHttpRequest: Boolean,
                            default: Option[String],
                            source: Either[QueryParam, PathParam]) {
  def isQueryParam: Boolean = source.isLeft

  def isPathParam: Boolean = !isQueryParam
}

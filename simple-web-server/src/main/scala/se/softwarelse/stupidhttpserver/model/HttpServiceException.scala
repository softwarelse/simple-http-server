package se.softwarelse.stupidhttpserver.model

case class HttpServiceException(status: Int, message: String)
  extends RuntimeException(s"HttpServiceException: status=$status, message=$message"){

}

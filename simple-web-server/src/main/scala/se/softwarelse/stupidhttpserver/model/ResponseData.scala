package se.softwarelse.stupidhttpserver.model

import java.nio.charset.StandardCharsets

case class ResponseData(data: Array[Byte], contentType: String) {

}

object ResponseData {

  def deduceType(path: String, data: String): ResponseData = {
    val lcPath = path.trim.toLowerCase
    if (lcPath.endsWith(".json")) {
      ResponseData.json(data)
    }
    else if (lcPath.endsWith(".js")) {
      ResponseData.js(data)
    }
    else if (lcPath.endsWith(".htm") || lcPath.endsWith(".html")) {
      ResponseData.html(data)
    }
    else {
      ResponseData.bin(data.getBytes(StandardCharsets.UTF_8))
    }
  }

  def deduceType(path: String, data: Array[Byte]): ResponseData = {
    val lcPath = path.trim.toLowerCase
    if (lcPath.endsWith(".json")) {
      ResponseData.json(data)
    }
    else if (lcPath.endsWith(".js")) {
      ResponseData.js(data)
    }
    else if (lcPath.endsWith(".htm") || lcPath.endsWith(".html")) {
      ResponseData.html(data)
    }
    else {
      ResponseData.bin(data)
    }
  }

  def bin(data: Array[Byte]): ResponseData = new ResponseData(data, "application/octet-stream")
  def json(data: String): ResponseData = new ResponseData(data.getBytes(StandardCharsets.UTF_8), "application/json")
  def json(data: Array[Byte]): ResponseData = new ResponseData(data, "application/json")
  def html(data: String): ResponseData = new ResponseData(data.getBytes(StandardCharsets.UTF_8), "text/html")
  def html(data: Array[Byte]): ResponseData = new ResponseData(data, "text/html")
  def js(data: String): ResponseData = new ResponseData(data.getBytes(StandardCharsets.UTF_8), "application/javascript")
  def js(data: Array[Byte]): ResponseData = new ResponseData(data, "application/javascript")
}
package se.softwarelse.stupidhttpserver

case class KV(key: String, value: String) {
  def isEmpty: Boolean = key.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def toJson: String = {
    s"""{ "${escapeJson(key)}": "${escapeJson(value)}" }"""
  }
}

package se.softwarelse.stupidhttpserver.model

case class KV(key: String, value: String) {
  def isEmpty: Boolean = key.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

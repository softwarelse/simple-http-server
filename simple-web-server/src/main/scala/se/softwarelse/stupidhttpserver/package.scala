package se.softwarelse

package object stupidhttpserver {

  val cr: String = "\r"
  val lf: String = "\n"
  val httpEndl: String = cr + lf

  def escapeJson(unescaped: String): String = {
    new String(unescaped.flatMap {
      case '"' => Seq('\\', '"')
      case '\\' => Seq('\\', '\\')
      case '/' => Seq('\\', '/')
      case '\b' => Seq('\\', 'b')
      case '\f' => Seq('\\', 'f')
      case '\n' => Seq('\\', 'n')
      case '\r' => Seq('\\', 'r')
      case '\t' => Seq('\\', 't')
      case x => Seq(x)
    }.toArray)
  }
}

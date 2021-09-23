package se.softwarelse.stupidhttpserver

import java.lang.annotation.Annotation

package object context {
  implicit class findAnnotationInList(list: Seq[Annotation]) {

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

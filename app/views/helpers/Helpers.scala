package views.html.helpers

import play.api.mvc.RequestHeader
import play.twirl.api.Html

object Helpers {
  def isActive(call: play.api.mvc.Call)(implicit req: RequestHeader): Boolean = req.path == call.toString
  def hasBaseUrl(call: play.api.mvc.Call)(implicit req: RequestHeader): Boolean = req.path.startsWith(call.toString)
}

object repeatWithIndex {
  // from https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/views/helper/Helpers.scala
  def apply(field: play.api.data.Field, min: Int = 1)(fieldRenderer: (Int, play.api.data.Field) => Html): Seq[Html] = {
    val indexes = field.indexes match {
      case Nil => 0 until min
      case complete if complete.size >= min => field.indexes
      case partial =>
        // We don't have enough elements, append indexes starting from the largest
        val start = field.indexes.max + 1
        val needed = min - field.indexes.size
        field.indexes ++ (start until (start + needed))
    }

    indexes.map(i => fieldRenderer(i, field("[" + i + "]")))
  }
}

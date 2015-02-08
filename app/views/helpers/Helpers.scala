package views.html.helpers

import common.models.Color
import play.api.data.Field
import play.api.mvc.RequestHeader
import play.twirl.api.Html

object Helpers {
  def isActive(call: play.api.mvc.Call)(implicit req: RequestHeader): Boolean = req.path == call.toString
  def hasBaseUrl(call: play.api.mvc.Call)(implicit req: RequestHeader): Boolean = req.path.startsWith(call.toString)
  def isActive(url: String)(implicit req: RequestHeader): Boolean = req.path == url
  def hasBaseUrl(url: String)(implicit req: RequestHeader): Boolean = req.path.startsWith(url)

  def isRequired(field: Field): Boolean = field.constraints.find { case (name, args) => name == "constraint.required" }.isDefined

  def getArg(args: Seq[(Symbol, String)], arg: String, default: String = ""): String = args.map { case (symbol, value) => (symbol.name, value) }.toMap.get(arg).getOrElse(default)

  def hasArg(args: Seq[(Symbol, String)], arg: String, expectedValue: String = ""): Boolean =
    args.map { case (symbol, value) => (symbol.name, value) }.toMap.get(arg).map { argValue =>
      argValue == "" || argValue == expectedValue
    }.getOrElse(false)

  def toHtmlArgs(args: Seq[(Symbol, String)], exclude: Seq[String] = Seq()): Html =
    Html(args
      .filter(e => !exclude.contains(e._1.name))
      .map { case (symbol, value) => symbol.name + "=\"" + value + "\"" }
      .mkString(" "))
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

object colors {
  def darker(color: String, k: Int = 1): String = {
    Color.parse(color).map {
      _.darker(k).toHexa
    }.getOrElse(color)
  }

  def lighter(color: String, k: Int = 1): String = {
    Color.parse(color).map {
      _.lighter(k).toHexa
    }.getOrElse(color)
  }

  def withAlpha(color: String, alpha: Double): String = {
    Color.parse(color).map {
      _.withAlpha(alpha).toRgba
    }.getOrElse(color)
  }
}

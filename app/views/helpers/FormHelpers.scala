package views.html.helpers

import play.api.data.Field
import play.twirl.api.Html

object FormHelpers {
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

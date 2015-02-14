package controllers.admin

import play.api.mvc._
import play.api.libs.json._

object Users extends Controller {
  def list(p: Int = 1, f: String = "", o: String = "") = Action { implicit req =>
    Ok(views.html.Application.Admin.User.list())
  }
}

package controllers.admin

import play.api.mvc._
import play.api.libs.json._

object Admin extends Controller {
  def home = Action { implicit req =>
    Redirect(routes.Users.list())
  }
}

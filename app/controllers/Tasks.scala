package controllers

import common.models.Page
import domain.models.Task
import domain.models.TaskData
import domain.repository.TaskRepository
import domain.services.TaskSrv
import play.api.mvc._
import play.api.data.Form
import play.api.libs.json.Json

object Tasks extends Controller {
  val form: Form[TaskData] = Form(TaskData.fields)
  val EltData = TaskData
  val repository = TaskRepository
  val srv = TaskSrv
  val mainRoute = routes.Tasks
  val viewList = views.html.Application.Task.list
  val viewListAll = views.html.Application.Task.listAll
  val viewCreate = views.html.Application.Task.create
  val viewDetails = views.html.Application.Task.details
  val viewEdit = views.html.Application.Task.edit
  def successCreateFlash(elt: Task) = s"Task '${elt.title}' has been created"
  def errorCreateFlash(elt: TaskData) = s"Task '${elt.title}' can't be created"
  def successUpdateFlash(elt: Task) = s"Task '${elt.title}' has been modified"
  def errorUpdateFlash(elt: Task) = s"Task '${elt.title}' can't be modified"
  def successDeleteFlash(elt: Task) = s"Task '${elt.title}' has been deleted"

  def list(p: Int) = Action { implicit req =>
    val page = repository.findPage(p)
    if (page.totalPages < p)
      Redirect(mainRoute.list(page.totalPages))
    else
      Ok(viewList(page))
  }

  def listAll = Action { implicit req =>
    Ok(viewListAll(repository.findAll()))
  }

  def create = Action { implicit req =>
    Ok(viewCreate(form))
  }

  def save = Action { implicit req =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(viewCreate(formWithErrors)),
      formData => {
        srv.create(formData).map { elt =>
          Redirect(mainRoute.list()).flashing("success" -> successCreateFlash(elt))
        }.getOrElse(InternalServerError(viewCreate(form.fill(formData))).flashing("error" -> errorCreateFlash(formData)))
      })
  }

  def details(uuid: String) = Action { implicit req =>
    repository.findById(uuid).map { elt =>
      Ok(viewDetails(elt))
    }.getOrElse(NotFound(views.html.error404()))
  }

  def edit(uuid: String) = Action { implicit req =>
    repository.findById(uuid).map { elt =>
      Ok(viewEdit(form.fill(EltData.fromModel(elt)), elt))
    }.getOrElse(NotFound(views.html.error404()))
  }

  def update(uuid: String) = Action { implicit req =>
    repository.findById(uuid).map { elt =>
      form.bindFromRequest.fold(
        formWithErrors => BadRequest(viewEdit(formWithErrors, elt)),
        formData => {
          srv.update(uuid, formData).map { updatedElt =>
            Redirect(mainRoute.list()).flashing("success" -> successUpdateFlash(updatedElt))
          }.getOrElse(InternalServerError(viewEdit(form.fill(formData), elt)).flashing("error" -> errorUpdateFlash(elt)))
        })
    }.getOrElse(NotFound(views.html.error404()))
  }

  def delete(uuid: String) = Action { implicit req =>
    repository.findById(uuid).map { elt =>
      repository.delete(uuid)
      Redirect(mainRoute.list()).flashing("success" -> successDeleteFlash(elt))
    }.getOrElse(NotFound(views.html.error404()))
  }

  def apiList(p: Int) = Action {
    Ok(Json.toJson(repository.findPage(p)))
  }
  def apiListAll = Action {
    Ok(Json.toJson(repository.findAll()))
  }
  def apiCreate = Action(parse.json) { req =>
    req.body.validate[TaskData].map { formData =>
      srv.create(formData).map { elt =>
        Ok(Json.toJson(elt))
      }.getOrElse(InternalServerError)
    }.getOrElse(BadRequest)
  }
  def apiDetails(uuid: String) = Action {
    repository.findById(uuid).map { elt =>
      Ok(Json.obj("item" -> elt))
    }.getOrElse(NotFound)
  }
  def apiUpdate(uuid: String) = Action(parse.json) { req =>
    repository.findById(uuid).map { elt =>
      req.body.validate[TaskData].map { formData =>
        repository.update(uuid, EltData.toModel(formData)).map { updatedElt =>
          Ok(Json.obj("item" -> updatedElt))
        }.getOrElse(InternalServerError)
      }.getOrElse(BadRequest)
    }.getOrElse(NotFound)
  }
  def apiDelete(uuid: String) = Action {
    repository.findById(uuid).map { elt =>
      repository.delete(uuid)
      Ok
    }.getOrElse(NotFound)
  }
}

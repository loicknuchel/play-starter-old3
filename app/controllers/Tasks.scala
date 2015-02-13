package controllers

import common.models.Repository
import common.models.MonadicResult
import common.controllers.ApiCrudUtils
import domain.models.Task
import domain.models.TaskData
import domain.repository.TaskRepository
import play.api.mvc._
import play.api.data.Form
import play.api.libs.json._

object Tasks extends Controller {
  val form: Form[TaskData] = Form(TaskData.fields)
  val repository: Repository[Task] = TaskRepository
  val mainRoute = routes.Tasks
  val viewList = views.html.Application.Task.list
  val viewListAll = views.html.Application.Task.listAll
  val viewCreate = views.html.Application.Task.create
  val viewDetails = views.html.Application.Task.details
  val viewEdit = views.html.Application.Task.edit
  def createElt(data: TaskData): Task = TaskData.toModel(data)
  def updateElt(elt: Task, data: TaskData): Task = elt.withData(data)
  def toData(elt: Task): TaskData = TaskData.fromModel(elt)
  def validate(json: JsValue): JsResult[TaskData] = json.validate[TaskData]
  def successCreateFlash(elt: Task) = s"Task '${elt.title}' has been created"
  def errorCreateFlash(elt: TaskData) = s"Task '${elt.title}' can't be created"
  def successUpdateFlash(elt: Task) = s"Task '${elt.title}' has been modified"
  def errorUpdateFlash(elt: Task) = s"Task '${elt.title}' can't be modified"
  def successDeleteFlash(elt: Task) = s"Task '${elt.title}' has been deleted"

  def list(p: Int = 1, f: String = "", o: String = "") = Action.async { implicit req =>
    repository.findPage(p, f, o).map { page =>
      if (p > 1 && page.totalPages < p)
        Redirect(mainRoute.list(page.totalPages, f, o))
      else
        Ok(viewList(page))
    }.get
  }

  def listAll(f: String = "", o: String = "") = Action.async { implicit req =>
    repository.findAll(f, o).map { all => Ok(viewListAll(all)) }.get
  }

  def create = Action { implicit req =>
    Ok(viewCreate(form))
  }

  def save = Action.async { implicit req =>
    form.bindFromRequest.fold(
      formWithErrors => MonadicResult(BadRequest(viewCreate(formWithErrors))),
      formData => repository.insert(createElt(formData)).map {
        _.map { elt =>
          Redirect(mainRoute.list()).flashing("success" -> successCreateFlash(elt))
        }.getOrElse(InternalServerError(viewCreate(form.fill(formData))).flashing("error" -> errorCreateFlash(formData)))
      }).get
  }

  def details(uuid: String) = Action.async { implicit req =>
    repository.findByUuid(uuid).map {
      _.map { elt =>
        Ok(viewDetails(elt))
      }.getOrElse(NotFound(views.html.error404()))
    }.get
  }

  def edit(uuid: String) = Action.async { implicit req =>
    repository.findByUuid(uuid).map {
      _.map { elt =>
        Ok(viewEdit(form.fill(toData(elt)), elt))
      }.getOrElse(NotFound(views.html.error404()))
    }.get
  }

  def update(uuid: String) = Action.async { implicit req =>
    repository.findByUuid(uuid).flatMap {
      _.map { elt =>
        form.bindFromRequest.fold(
          formWithErrors => MonadicResult(BadRequest(viewEdit(formWithErrors, elt))),
          formData => repository.update(uuid, updateElt(elt, formData)).map {
            _.map { updatedElt =>
              Redirect(mainRoute.list()).flashing("success" -> successUpdateFlash(updatedElt))
            }.getOrElse(InternalServerError(viewEdit(form.fill(formData), elt)).flashing("error" -> errorUpdateFlash(elt)))
          })
      }.getOrElse(MonadicResult(NotFound(views.html.error404())))
    }.get
  }

  def delete(uuid: String) = Action.async { implicit req =>
    repository.findByUuid(uuid).map {
      _.map { elt =>
        repository.delete(uuid)
        Redirect(mainRoute.list()).flashing("success" -> successDeleteFlash(elt))
      }.getOrElse(NotFound(views.html.error404()))
    }.get
  }

  val crudApi = ApiCrudUtils(repository, Task.format, validate, createElt, updateElt)

  def apiList = crudApi.list
  def apiListAll = crudApi.listAll
  def apiCreate = crudApi.create
  def apiDetails = crudApi.details
  def apiUpdate = crudApi.update
  def apiDelete = crudApi.delete
}

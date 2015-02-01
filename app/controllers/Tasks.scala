package controllers

import common.models.Page
import domain.models.Task
import domain.models.TaskData
import domain.repository.TaskRepository
import play.api.mvc._
import play.api.data.Form
import play.api.libs.json.Json

object Tasks extends Controller {
  val taskForm = Form(TaskData.fields)

  def list(p: Int) = Action { implicit req =>
    val page = TaskRepository.findPage(p)
    if (page.totalPages < p)
      Redirect(routes.Tasks.list(page.totalPages))
    else
      Ok(views.html.Application.Task.list(page))
  }

  def listAll = Action { implicit req =>
    Ok(views.html.Application.Task.listAll(TaskRepository.findAll()))
  }

  def create = Action { implicit req =>
    Ok(views.html.Application.Task.create(taskForm))
  }

  def save = Action { implicit req =>
    taskForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.Application.Task.create(formWithErrors)),
      formData => {
        TaskRepository.insert(Task.fromData(formData))
        Redirect(routes.Tasks.list()).flashing("success" -> s"Task '${formData.title}' has been created")
      })
  }

  def details(uuid: String) = Action { implicit req =>
    TaskRepository.findById(uuid).map { task =>
      Ok(views.html.Application.Task.details(task))
    }.getOrElse(NotFound(views.html.error404()))
  }

  def edit(uuid: String) = Action { implicit req =>
    TaskRepository.findById(uuid).map { task =>
      Ok(views.html.Application.Task.edit(taskForm.fill(Task.toData(task)), task))
    }.getOrElse(NotFound(views.html.error404()))
  }

  def update(uuid: String) = Action { implicit req =>
    TaskRepository.findById(uuid).map { task =>
      taskForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.Application.Task.edit(formWithErrors, task)),
        formData => {
          TaskRepository.update(uuid, Task.fromData(formData))
          Redirect(routes.Tasks.list()).flashing("success" -> s"Task '${formData.title}' has been modified")
        })
    }.getOrElse(NotFound(views.html.error404()))
  }

  def delete(uuid: String) = Action { implicit req =>
    TaskRepository.findById(uuid).map { task =>
      TaskRepository.delete(uuid)
      Redirect(routes.Tasks.list()).flashing("success" -> s"Task '${task.title}' has been deleted")
    }.getOrElse(NotFound(views.html.error404()))
  }

  def apiList(p: Int) = Action {
    Ok // TODO : serialize Page[A] to json...
  }
  def apiListAll = Action {
    Ok(Json.toJson(TaskRepository.findAll()))
  }
  def apiCreate = Action(parse.json) { req =>
    req.body.validate[TaskData].map { formData =>
      val task = Task.fromData(formData)
      TaskRepository.insert(task)
      Ok(Json.toJson(task))
    }.getOrElse(BadRequest)
  }
  def apiDetails(uuid: String) = Action {
    TaskRepository.findById(uuid).map { task =>
      Ok(Json.toJson(task))
    }.getOrElse(NotFound)
  }
  def apiUpdate(uuid: String) = Action(parse.json) { req =>
    TaskRepository.findById(uuid).map { task =>
      req.body.validate[TaskData].map { formData =>
        TaskRepository.update(uuid, Task.fromData(formData)).map { task =>
          Ok(Json.toJson(task))
        }.getOrElse(InternalServerError)
      }.getOrElse(BadRequest)
    }.getOrElse(NotFound)
  }
  def apiDelete(uuid: String) = Action {
    TaskRepository.findById(uuid).map { task =>
      TaskRepository.delete(uuid)
      Ok
    }.getOrElse(NotFound)
  }
}

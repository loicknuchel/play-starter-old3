package controllers

import domain.models.Task
import domain.models.TaskData
import domain.repository.TaskRepository
import play.api.mvc._
import play.api.data.Form

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
}
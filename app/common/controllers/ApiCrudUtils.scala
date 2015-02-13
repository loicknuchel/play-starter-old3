package common.controllers

import common.models.Page
import common.models.Repository
import common.models.MonadicResult
import domain.models.Task
import domain.models.TaskData
import play.api.mvc._
import play.api.mvc.BodyParsers._
import play.api.libs.json._

case class ApiCrudUtils[T, TData](
  repository: Repository[T],
  format: Format[T],
  validate: JsValue => JsResult[TData],
  createElt: TData => T,
  updateElt: (T, TData) => T) {
  implicit val w: Writes[T] = format
  implicit val f: Format[T] = format
  implicit val pf: Format[Page[T]] = Page.format
  def list: (Int, String, String) => Action[AnyContent] = ApiCrudUtils.list(repository)
  def listAll: (String, String) => Action[AnyContent] = ApiCrudUtils.listAll(repository)
  def create: Action[JsValue] = ApiCrudUtils.create(repository, validate, createElt)
  def details: (String) => Action[AnyContent] = ApiCrudUtils.details(repository)
  def update: (String) => Action[JsValue] = ApiCrudUtils.update(repository, validate, updateElt)
  def delete: (String) => Action[AnyContent] = ApiCrudUtils.delete(repository)
}
object ApiCrudUtils {
  def list[T](repository: Repository[T])(p: Int = 1, f: String = "", o: String = "")(implicit w: Writes[Page[T]]) = Action.async {
    repository.findPage(p, f, o).map { page =>
      Results.Ok(Json.toJson(page))
    }.get
  }

  def listAll[T](repository: Repository[T])(f: String = "", o: String = "")(implicit w: Writes[Page[T]]) = Action.async {
    repository.findAll(f, o).map { all =>
      Results.Ok(Json.toJson(Page(all, 1,  all.size,  all.size)))
    }.get
  }

  def create[T, TData](repository: Repository[T], validate: JsValue => JsResult[TData], createElt: TData => T)(implicit w: Writes[T]) = Action.async(parse.json) { req =>
    validate(req.body).map { formData =>
      repository.insert(createElt(formData)).map {
        _.map { elt =>
          Results.Ok(Json.obj("item" -> elt))
        }.getOrElse(Results.InternalServerError)
      }.get
    }.getOrElse(MonadicResult(Results.BadRequest).get)
  }

  def details[T](repository: Repository[T])(uuid: String)(implicit w: Writes[T]) = Action.async {
    repository.findByUuid(uuid).map {
      _.map { elt =>
        Results.Ok(Json.obj("item" -> elt))
      }.getOrElse(Results.NotFound)
    }.get
  }

  def update[T, TData](repository: Repository[T], validate: JsValue => JsResult[TData], updateElt: (T, TData) => T)(uuid: String)(implicit w: Writes[T]) = Action.async(parse.json) { req =>
    repository.findByUuid(uuid).flatMap { u =>
      val a: MonadicResult[Result] = u.map { elt =>
        val b: MonadicResult[Result] = validate(req.body).map { formData =>
          repository.update(uuid, updateElt(elt, formData)).map {
            _.map { updatedElt =>
              Results.Ok(Json.obj("item" -> updatedElt))
            }.getOrElse(Results.InternalServerError)
          }
        }.getOrElse(MonadicResult(Results.BadRequest))
        b
      }.getOrElse(MonadicResult(Results.NotFound))
      a
    }.get
  }

  def delete[T](repository: Repository[T])(uuid: String) = Action.async {
    repository.findByUuid(uuid).map {
      _.map { elt =>
        repository.delete(uuid)
        Results.Ok
      }.getOrElse(Results.NotFound)
    }.get
  }
}
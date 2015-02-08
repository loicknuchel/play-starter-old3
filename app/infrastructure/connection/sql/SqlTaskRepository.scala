package infrastructure.connection.sql

import common.models.Page
import common.models.UUID
import common.models.MonadicResult
import common.models.SyncResult
import common.models.Repository
import common.infrastructure.SqlCrudUtils
import domain.models.Task
import anorm._
import anorm.SqlParser._

trait SqlTaskRepository extends Repository[Task] {
  private val tableName = "Tasks"

  private val rowParser = {
    get[String](s"$tableName.uuid") ~
      get[String](s"$tableName.title") ~
      get[String](s"$tableName.description") ~
      get[Boolean](s"$tableName.done") map {
        case uuid ~ title ~ description ~ done =>
          Task(uuid, title, description, done)
      }
  }

  private def toValues(task: Option[Task]): List[(String, ParameterValue)] = {
    List(
      ("uuid", task.map(_.uuid)),
      ("title", task.map(_.title)),
      ("description", task.map(_.description)),
      ("done", task.map(_.done)))
  }

  private val crud = SqlCrudUtils(tableName, toValues, rowParser, "uuid")

  override def findAll(): MonadicResult[List[Task]] = SyncResult(crud.findAll())
  override def findPage(page: Int): MonadicResult[Page[Task]] = SyncResult(crud.findPage(page, orderBy = Some("title")))
  override def findByUuid(uuid: UUID): MonadicResult[Option[Task]] = SyncResult(crud.findById(uuid))
  override def insert(elt: Task): MonadicResult[Option[Task]] = SyncResult(crud.insert(elt.withUuid(generateUuid())).map(s => elt))
  override def update(uuid: UUID, elt: Task): MonadicResult[Option[Task]] = { crud.update(uuid, elt); SyncResult(Some(elt)) }
  override def delete(uuid: UUID): MonadicResult[Option[Task]] = { crud.delete(uuid); SyncResult(None); }
}

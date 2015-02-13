package infrastructure.connection.sql

import common.models.Page
import common.models.UUID
import common.models.MonadicResult
import common.models.SyncResult
import common.models.Repository
import common.infrastructure.SqlCrudUtils
import domain.models.Task
import java.util.Date
import anorm._
import anorm.SqlParser._

trait SqlTaskRepository extends Repository[Task] {
  private val tableName = TableReferences.TASKS

  private val rowParser = {
    get[String](s"$tableName.uuid") ~
      get[String](s"$tableName.title") ~
      get[String](s"$tableName.description") ~
      get[Boolean](s"$tableName.done") ~
      get[Date](s"$tableName.created") ~
      get[Date](s"$tableName.updated") map {
        case uuid ~ title ~ description ~ done ~ created ~ updated =>
          Task(uuid, title, description, done, created, updated)
      }
  }

  private def toValues(task: Option[Task]): List[(String, ParameterValue)] = {
    List(
      ("uuid", task.map(_.uuid)),
      ("title", task.map(_.title)),
      ("description", task.map(_.description)),
      ("done", task.map(_.done)),
      ("created", task.map(_.created)),
      ("updated", task.map(_.updated)))
  }

  private val crud = SqlCrudUtils(tableName, toValues, rowParser, List("title", "description"), "uuid")

  override def findAll(filter: String = "", orderBy: String = ""): MonadicResult[List[Task]] = SyncResult(crud.findAll(filter, orderBy))
  override def findPage(page: Int = 1, filter: String = "", orderBy: String = ""): MonadicResult[Page[Task]] = SyncResult(crud.findPage(page, filter, orderBy))
  override def findByUuid(uuid: UUID): MonadicResult[Option[Task]] = SyncResult(crud.findByUuid(uuid))
  override def insert(elt: Task): MonadicResult[Option[Task]] = SyncResult({ crud.insert(elt).map(s => elt) })
  override def update(uuid: UUID, elt: Task): MonadicResult[Option[Task]] = SyncResult({ crud.update(uuid, elt); Some(elt) })
  override def delete(uuid: UUID): MonadicResult[Option[Task]] = SyncResult({ crud.delete(uuid); None }) // TODO : return deleted elt !
}

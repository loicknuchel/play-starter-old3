package infrastructure.connection.sql

import common.models.Page
import common.infrastructure.SqlCrudUtils
import domain.models.Task
import domain.repository.UUID
import domain.repository.SyncResult
import domain.repository.TaskRepository
import anorm._
import anorm.SqlParser._

trait SqlTaskRepository extends TaskRepository[SyncResult] {
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

  /*
  def findAll(): List[Task] = crud.findAll()
  def findPage(page: Int = 1, filter: String = "%%", orderBy: Int = 1, pageSize: Int = 10): Page[Task] = crud.findPage(page, filter, orderBy, pageSize)
  def findById(uid: String): Option[Task] = crud.findById(uid)
  def findByIds(uids: Seq[String]): List[Task] = crud.findByIds(uids)
  def findBy(fieldName: String, fieldValue: String): Option[Task] = crud.findBy(fieldName, fieldValue)
  def findBy(fieldName: String, fieldValues: Seq[String]): List[Task] = crud.findBy(fieldName, fieldValues)
  def insert(data: Task): Option[String] = crud.insert(data)
  def update(uid: String, data: Task): Int = crud.update(uid, data)
  def delete(uid: String): Int = crud.delete(uid)
  */

  def findAll(): List[Task] = crud.findAll
  def findPage: Int => Page[Task] = page => crud.findPage(page, orderBy = Some("title"))
  def findById: UUID => Option[Task] = uuid => crud.findById(uuid)
  def insert: Task => Option[Task] = task => crud.insert(task).map(s => task)
  def update: (UUID, Task) => Option[Task] = (uuid, task) => { crud.update(uuid, task); Some(task) }
  def delete: UUID => Option[Task] = uuid => { crud.delete(uuid); None; }
}
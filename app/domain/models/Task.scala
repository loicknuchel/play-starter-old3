package domain.models

import common.models.UUID
import common.models.Repository
import java.util.Date
import play.api.data.Forms._
import play.api.libs.json.Json

case class Task(
  uuid: UUID,
  title: String,
  description: String,
  done: Boolean,
  created: Date,
  updated: Date) {
  // TODO : add createdBy, updatedBy fields
  def withData(d: TaskData) = this.copy(title = d.title, description = d.description, done = d.done, updated = new Date())
}
object Task {
  implicit val format = Json.format[Task]
}

// mapping object for Task Form
case class TaskData(
  title: String,
  description: String,
  done: Boolean)
object TaskData {
  implicit val format = Json.format[TaskData]
  val fields = mapping(
    "title" -> nonEmptyText,
    "description" -> text,
    "done" -> boolean)(TaskData.apply)(TaskData.unapply)

  def toModel(d: TaskData): Task = Task(Repository.generateUuid(), d.title, d.description, d.done, new Date(), new Date())
  def fromModel(t: Task): TaskData = TaskData(t.title, t.description, t.done)
}

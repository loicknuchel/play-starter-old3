package domain.models

import domain.repository.UUID
import play.api.data.Forms._
import play.api.libs.json.Json

case class Task(
  uuid: UUID,
  title: String,
  description: String,
  done: Boolean)
object Task {
  implicit val format = Json.format[Task]
}

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

  def toModel(d: TaskData): Task = Task(java.util.UUID.randomUUID().toString(), d.title, d.description, d.done)
  def fromModel(t: Task): TaskData = TaskData(t.title, t.description, t.done)
}

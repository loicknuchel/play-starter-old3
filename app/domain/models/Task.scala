package domain.models

import play.api.data.Forms._

case class TaskData(
  title: String,
  description: String,
  done: Boolean)
object TaskData {
  val fields = mapping(
    "title" -> nonEmptyText,
    "description" -> text,
    "done" -> boolean)(TaskData.apply)(TaskData.unapply)
}

case class Task(
  uuid: String,
  title: String,
  description: String,
  done: Boolean)
object Task {
  def fromData(d: TaskData): Task = Task(java.util.UUID.randomUUID().toString(), d.title, d.description, d.done)
  def toData(t: Task): TaskData = TaskData(t.title, t.description, t.done)
}

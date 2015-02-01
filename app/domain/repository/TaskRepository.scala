package domain.repository

import common.models.Page
import domain.models.Task
import infrastructure.connection.sql.SqlTaskRepository

trait TaskRepository[ResultWrapper[_]] {
  def findAll: ResultWrapper[List[Task]]
  def findPage: Int => ResultWrapper[Page[Task]]
  def findById: UUID => ResultWrapper[Option[Task]]
  def insert: Task => ResultWrapper[Option[Task]]
  def update: (UUID, Task) => ResultWrapper[Option[Task]]
  def delete: UUID => ResultWrapper[Option[Task]]
}
object TaskRepository extends SqlTaskRepository

package infrastructure.connection.mongodb

import common.models.Page
import domain.models.Task
import domain.repository.{ UUID, TaskRepository }
import infrastructure.binding.json.TaskFormat._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.api.DB
import play.modules.reactivemongo.json.collection.JSONCollection

import scalaz._
import Scalaz._

trait MongoDbTaskRepository extends TaskRepository[Future] with Connection {
  override lazy val collection: JSONCollection = db[JSONCollection](CollectionReference.TASKS)

  override def findAll: Future[List[Task]] = collection.find(Json.obj()).cursor[Task].collect[List]()
  override def findPage: Int => Future[Page[Task]] = page => Future.successful(Page(List(), 0, 0, 0)) // TODO
  override def findById: UUID => Future[Option[Task]] = uuid => collection.find(Json.obj("uuid" -> uuid)).cursor[Task].headOption
  override def insert: Task => Future[Option[Task]] = task => collection.save(task).map { l => if (l.ok) task.some else None }
  override def update: (UUID, Task) => Future[Option[Task]] = (uuid, task) => collection.update(Json.obj("uuid" -> uuid), task).map { l => if (l.updated == 1) task.some else None }
  override def delete: UUID => Future[Option[Task]] = uuid => Future.successful(None) // TODO
}

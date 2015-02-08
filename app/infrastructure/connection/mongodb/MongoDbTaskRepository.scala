package infrastructure.connection.mongodb

import common.models.Page
import common.models.UUID
import common.models.MonadicResult
import common.models.AsyncResult
import common.models.Repository
import domain.models.Task
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.api.DB
import play.modules.reactivemongo.json.collection.JSONCollection

trait MongoDbTaskRepository extends Repository[Task] with Connection {
  override lazy val collection: JSONCollection = db[JSONCollection](CollectionReferences.TASKS)

  override def findAll(): MonadicResult[List[Task]] = AsyncResult(collection.find(Json.obj()).cursor[Task].collect[List]())
  override def findPage(page: Int): MonadicResult[Page[Task]] = AsyncResult(Page(List[Task](), 0, 0, 0)) // TODO
  override def findByUuid(uuid: UUID): MonadicResult[Option[Task]] = AsyncResult(collection.find(Json.obj("uuid" -> uuid)).cursor[Task].headOption)
  override def insert(elt: Task): MonadicResult[Option[Task]] = AsyncResult(collection.save(elt.withUuid(generateUuid())).map { lastError => if (lastError.ok) Some(elt.withUuid(generateUuid())) else None })
  override def update(uuid: UUID, elt: Task): MonadicResult[Option[Task]] = AsyncResult(collection.update(Json.obj("uuid" -> uuid), elt).map { lastError => if (lastError.updated == 1) Some(elt) else None })
  override def delete(uuid: UUID): MonadicResult[Option[Task]] = AsyncResult(None) // TODO
}

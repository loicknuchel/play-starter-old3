package infrastructure.connection.mongodb

import common.models.Page
import common.models.UUID
import common.models.MonadicResult
import common.models.AsyncResult
import common.models.Repository
import domain.models.Task
import common.infrastructure.MongoDbCrudUtils
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.api.DB
import play.modules.reactivemongo.json.collection.JSONCollection

trait MongoDbTaskRepository extends Repository[Task] with Connection {
  override lazy val collection: JSONCollection = db[JSONCollection](CollectionReferences.TASKS)

  private val crud = MongoDbCrudUtils(collection, Task.format, List("title", "description"), "uuid")

  override def findAll(filter: String = "", orderBy: String = ""): MonadicResult[List[Task]] = AsyncResult(crud.findAll(filter, orderBy))
  override def findPage(page: Int = 1, filter: String = "", orderBy: String = ""): MonadicResult[Page[Task]] = AsyncResult(crud.findPage(page, filter, orderBy))
  override def findByUuid(uuid: UUID): MonadicResult[Option[Task]] = AsyncResult(crud.findByUuid(uuid))
  override def insert(elt: Task): MonadicResult[Option[Task]] = AsyncResult({ val eltWithId = elt.withUuid(generateUuid()); crud.insert(eltWithId).map(err => if (err.ok) Some(eltWithId) else None) })
  override def update(uuid: UUID, elt: Task): MonadicResult[Option[Task]] = AsyncResult(crud.update(uuid, elt).map(err => if (err.ok) Some(elt) else None))
  override def delete(uuid: UUID): MonadicResult[Option[Task]] = AsyncResult(crud.delete(uuid).map(err => None)) // TODO : return deleted elt !
}

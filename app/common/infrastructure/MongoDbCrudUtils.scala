package common.infrastructure

import common.Defaults
import common.models.Page
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.core.commands.LastError
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.Count
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONArray

/*
 * Convention :
 *  - methods get* return one result (Option[T])
 *  - methods find* return a list of results (List[T])
 */
case class MongoDbCrudUtils[T](
  collection: JSONCollection,
  format: Format[T],
  filterFields: List[String] = Nil,
  fieldUuid: String = "uuid") {
  implicit val r: Reads[T] = format
  implicit val w: Writes[T] = format
  def findAll(filter: String = "", orderBy: String = ""): Future[List[T]] = MongoDbCrudUtils.findAll(collection, filter, filterFields, orderBy)
  def findPage(page: Int = 1, filter: String = "", orderBy: String = "", pageSize: Int = Defaults.pageSize): Future[Page[T]] = MongoDbCrudUtils.findPage(collection, page, filter, filterFields, orderBy, pageSize)
  def findBy(fieldName: String, fieldValue: String): Future[List[T]] = MongoDbCrudUtils.findBy(fieldValue, collection, fieldName)
  def getByUuid(uuid: String): Future[Option[T]] = MongoDbCrudUtils.getBy(uuid, collection, fieldUuid)
  def findByUuids(uuids: Seq[String]): Future[List[T]] = MongoDbCrudUtils.findByList(uuids, collection, fieldUuid)
  def getBy(fieldName: String, fieldValue: String): Future[Option[T]] = MongoDbCrudUtils.getBy(fieldValue, collection, fieldName)
  def findBy(fieldName: String, fieldValues: Seq[String]): Future[List[T]] = MongoDbCrudUtils.findByList(fieldValues, collection, fieldName)
  def insert(elt: T): Future[LastError] = MongoDbCrudUtils.insert(elt, collection)
  def update(uuid: String, elt: T): Future[LastError] = MongoDbCrudUtils.update(uuid, elt, collection, fieldUuid)
  def delete(uuid: String): Future[LastError] = MongoDbCrudUtils.delete(uuid, collection, fieldUuid)
}
object MongoDbCrudUtils {
  def findAll[T](collection: JSONCollection, filter: String = "", filterFields: List[String] = Nil, orderBy: String = "")(implicit r: Reads[T]): Future[List[T]] = {
    val mongoFilterJson = buildFilter(filter, filterFields)
    val mongoOrder = buildOrder(orderBy)

    collection.find(mongoFilterJson).sort(mongoOrder).cursor[T].collect[List]()
  }

  def findPage[T](collection: JSONCollection, page: Int = 1, filter: String = "", filterFields: List[String] = Nil, orderBy: String = "", pageSize: Int = Defaults.pageSize)(implicit r: Reads[T]): Future[Page[T]] = {
    val realPage = if (page > 1) page - 1 else 0
    val offset = pageSize * realPage

    val mongoFilterJson = buildFilter(filter, filterFields)
    val mongoFilter = BSONFormats.BSONDocumentFormat.reads(mongoFilterJson).get
    val mongoOrder = buildOrder(orderBy)

    for (
      items <- collection.find(mongoFilterJson).options(QueryOpts(offset, pageSize)).sort(mongoOrder).cursor[T].collect[List](pageSize);
      totalItems <- collection.db.command(Count(collection.name, Some(mongoFilter)))
    ) yield Page(items, realPage + 1, pageSize, totalItems)
  }

  def findBy[T](value: String, collection: JSONCollection, fieldName: String = "uuid")(implicit r: Reads[T]): Future[List[T]] = {
    collection.find(Json.obj(fieldName -> value)).cursor[T].collect[List]()
  }

  def getBy[T](uuid: String, collection: JSONCollection, fieldUuid: String = "uuid")(implicit r: Reads[T]): Future[Option[T]] = {
    collection.find(Json.obj(fieldUuid -> uuid)).one[T]
  }

  def findByList[T](uuids: Seq[String], collection: JSONCollection, fieldUuid: String = "uuid")(implicit r: Reads[T]): Future[List[T]] = {
    val mongoFilter = Json.obj("$or" -> uuids.map(uuid => Json.obj(fieldUuid -> uuid)))
    collection.find(mongoFilter).cursor[T].collect[List]()
  }

  def insert[T](elt: T, collection: JSONCollection)(implicit w: Writes[T]): Future[LastError] = {
    collection.save(elt)
  }

  def update[T](uuid: String, elt: T, collection: JSONCollection, fieldUuid: String = "uuid")(implicit w: Writes[T]): Future[LastError] = {
    collection.update(Json.obj(fieldUuid -> uuid), elt)
  }

  def delete(uuid: String, collection: JSONCollection, fieldUuid: String = "uuid"): Future[LastError] = {
    collection.remove(Json.obj(fieldUuid -> uuid))
  }

  private def buildFilter(filter: String, filterFields: List[String]): JsObject = {
    Json.obj("$or" -> filterFields.map(field => Json.obj(field -> Json.obj("$regex" -> (".*" + filter + ".*"), "$options" -> "i"))))
  }

  private def buildOrder(orderBy: String): JsObject = {
    val IsReverse = "(.)(.*)".r
    orderBy match {
      case IsReverse("-", v) => Json.obj(v -> -1)
      case IsReverse(v1, v2) => Json.obj((v1 + v2) -> 1)
      case v => Json.obj()
    }
  }
}

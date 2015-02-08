package common.infrastructure

import common.models.Page
import play.api.Play.current
import play.api.db.DB
import anorm._
import anorm.SqlParser._
import common.Defaults

case class SqlCrudUtils[T](
  tableName: String,
  toValues: Option[T] => List[(String, ParameterValue)],
  rowParser: RowParser[T],
  filterFields: List[String] = Nil,
  fieldUuid: String = "uuid") {
  def findAll(filter: String = "", orderBy: String = ""): List[T] = SqlCrudUtils.findAll(tableName, rowParser, filter, filterFields, orderBy)
  def findPage(page: Int = 1, filter: String = "", orderBy: String = "", pageSize: Int = Defaults.pageSize): Page[T] = SqlCrudUtils.findPage(tableName, rowParser, page, filter, filterFields, orderBy, pageSize)
  def findByUuid(uuid: String): Option[T] = SqlCrudUtils.findBy(uuid, tableName, rowParser, fieldUuid)
  def findByUuids(uuids: Seq[String]): List[T] = SqlCrudUtils.findByList(uuids, tableName, rowParser, fieldUuid)
  def findBy(fieldName: String, fieldValue: String): Option[T] = SqlCrudUtils.findBy(fieldValue, tableName, rowParser, fieldName)
  def findBy(fieldName: String, fieldValues: Seq[String]): List[T] = SqlCrudUtils.findByList(fieldValues, tableName, rowParser, fieldName)
  def insert(data: T): Option[String] = SqlCrudUtils.insert(toValues(Some(data)), tableName)
  def update(uuid: String, data: T): Int = SqlCrudUtils.update(uuid, toValues(Some(data)), tableName, fieldUuid)
  def delete(uuid: String): Int = SqlCrudUtils.delete(uuid, tableName, fieldUuid)
}
object SqlCrudUtils {
  def findAll[T](tableName: String, rowParser: RowParser[T], filter: String = "", filterFields: List[String] = Nil, orderBy: String = ""): List[T] = {
    DB.withConnection { implicit connection =>
      val realFilter = if (filter.length() == 0) "%%" else "%" + filter + "%"
      val sqlFilter = buildFilter(filterFields)
      val sqlOrder = buildOrder(orderBy)
      SQL(s"select * from $tableName $sqlFilter $sqlOrder").as(rowParser *)
    }
  }

  def findPage[T](tableName: String, rowParser: RowParser[T], page: Int = 1, filter: String = "", filterFields: List[String] = Nil, orderBy: String = "", pageSize: Int = Defaults.pageSize): Page[T] = {
    DB.withConnection { implicit connection =>
      val realPage = if (page > 1) page - 1 else 0
      val offset = pageSize * realPage
      val realFilter = if (filter.length() == 0) "%%" else "%" + filter + "%"
      val sqlFilter = buildFilter(filterFields)
      val sqlOrder = buildOrder(orderBy)
      val sqlLimit = "limit {pageSize} offset {offset}"
      val items = SQL(s"select * from $tableName $sqlFilter $sqlOrder $sqlLimit").on(
        'filter -> realFilter,
        'pageSize -> pageSize,
        'offset -> offset).as(rowParser *)
      val totalItems = SQL(s"select count(*) from $tableName $sqlFilter").on('filter -> realFilter).as(scalar[Long].single)
      Page(items, realPage + 1, pageSize, totalItems)
    }
  }

  def findBy[T](uuid: String, tableName: String, rowParser: RowParser[T], fieldUuid: String = "uuid"): Option[T] = {
    DB.withConnection { implicit connection =>
      SQL(s"select * from $tableName where $fieldUuid={uuid}").on('uuid -> uuid).as(rowParser.singleOpt)
    }
  }

  def findByList[T](uuids: Seq[String], tableName: String, rowParser: RowParser[T], fieldUuid: String = "uuid"): List[T] = {
    if (uuids.length > 0) {
      DB.withConnection { implicit connection =>
        val (fields, values) = uuids.zipWithIndex.map {
          case (uuid, i) => (s"{uuid$i}", NamedParameter(s"uuid$i", uuid))
        }.unzip
        val uuidFields = fields.mkString(", ")
        SQL(s"select * from $tableName where $fieldUuid IN ($uuidFields)").on(values: _*).as(rowParser *)
        List()
      }
    } else {
      List()
    }
  }

  def insert[T](values: List[(String, ParameterValue)], tableName: String): Option[String] = {
    DB.withConnection { implicit connection =>
      val fields = values.map(_._1)
      val params = values.map(t => NamedParameter(t._1, t._2))
      val tableFields = fields.mkString(", ")
      val tableFieldsValues = fields.map(str => "{" + str + "}").mkString(", ")
      SQL(s"insert into $tableName($tableFields) values ($tableFieldsValues)").on(params: _*).executeInsert().map(uuid => uuid.toString)
    }
  }

  def update[T](uuid: String, values: List[(String, ParameterValue)], tableName: String, fieldUuid: String = "uuid"): Int = {
    DB.withConnection { implicit connection =>
      val fields = values.map(_._1)
      val sqlFields = fields.map(str => s"$tableName.$str={$str}").mkString(", ")
      val valuesWithUuid = values ++ List(("uuid", uuid: ParameterValue))
      val params = valuesWithUuid.map(t => NamedParameter(t._1, t._2))
      SQL(s"update $tableName set $sqlFields where $fieldUuid={uuid}").on(params: _*).executeUpdate()
    }
  }

  def delete(uuid: String, tableName: String, fieldUuid: String = "uuid"): Int = {
    DB.withConnection { implicit connection =>
      SQL(s"delete from $tableName where $fieldUuid={uuid}").on('uuid -> uuid).executeUpdate()
    }
  }

  private def buildFilter(filterFields: List[String]): String = {
    if (filterFields.size == 0) ""
    else "where " + filterFields.map(str => s"$str like {filter}").mkString(" OR ")
  }

  private def buildOrder(orderBy: String): String = {
    val IsReverse = "(.)(.*)".r
    orderBy match {
      case IsReverse("-", v) => "order by " + v + " DESC"
      case IsReverse(v1, v2) => "order by " + v1 + v2
      case v => ""
    }
  }
}

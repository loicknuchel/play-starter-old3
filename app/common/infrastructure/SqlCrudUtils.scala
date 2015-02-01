package common.infrastructure

import common.models.Page
import play.api.Play.current
import play.api.db.DB
import anorm._
import anorm.SqlParser._

case class SqlCrudUtils[T](
  tableName: String,
  toValues: Option[T] => List[(String, ParameterValue)],
  rowParser: RowParser[T],
  fieldId: String = "id") {
  def findAll(): List[T] = SqlCrudUtils.findAll(tableName, rowParser)
  def findPage(page: Int = 1, filter: String = "%%", orderBy: Option[String] = None, pageSize: Int = 5): Page[T] = SqlCrudUtils.findPage(tableName, rowParser, page, filter, toValues(None).map(_._1), orderBy, pageSize)
  def findById(id: String): Option[T] = SqlCrudUtils.findBy(id, tableName, rowParser, fieldId)
  def findByIds(ids: Seq[String]): List[T] = SqlCrudUtils.findByList(ids, tableName, rowParser, fieldId)
  def findBy(fieldName: String, fieldValue: String): Option[T] = SqlCrudUtils.findBy(fieldValue, tableName, rowParser, fieldName)
  def findBy(fieldName: String, fieldValues: Seq[String]): List[T] = SqlCrudUtils.findByList(fieldValues, tableName, rowParser, fieldName)
  def insert(data: T): Option[String] = SqlCrudUtils.insert(toValues(Some(data)), tableName)
  def update(id: String, data: T): Int = SqlCrudUtils.update(id, toValues(Some(data)), tableName, fieldId)
  def delete(id: String): Int = SqlCrudUtils.delete(id, tableName, fieldId)
}
object SqlCrudUtils {
  def findAll[T](tableName: String, rowParser: RowParser[T]): List[T] = {
    DB.withConnection { implicit connection =>
      SQL(s"select * from $tableName").as(rowParser *)
    }
  }

  def findPage[T](tableName: String, rowParser: RowParser[T], page: Int = 1, filter: String = "%%", filterFields: List[String] = Nil, orderBy: Option[String] = None, pageSize: Int = 10): Page[T] = {
    DB.withConnection { implicit connection =>
      val realPage = if (page > 1) page - 1 else 0
      val offset = pageSize * realPage
      val realFilter = if (filter.length() == 0) "%%" else "%" + filter + "%"
      val sqlFilter = "where " + filterFields.map(str => s"$str like {filter}").mkString(" OR ")
      val sqlOrder = orderBy.map(o => "order by " + o).getOrElse("")
      val sqlLimit = "limit {pageSize} offset {offset}"
      val items = SQL(s"select * from $tableName $sqlFilter $sqlOrder $sqlLimit").on(
        'filter -> realFilter,
        'pageSize -> pageSize,
        'offset -> offset).as(rowParser *)
      val totalItems = SQL(s"select count(*) from $tableName $sqlFilter").on('filter -> realFilter).as(scalar[Long].single)
      Page(items, realPage + 1, pageSize, totalItems)
    }
  }

  def findBy[T](id: String, tableName: String, rowParser: RowParser[T], field: String = "id"): Option[T] = {
    DB.withConnection { implicit connection =>
      SQL(s"select * from $tableName where $field={id}").on('id -> id).as(rowParser.singleOpt)
    }
  }

  def findByList[T](ids: Seq[String], tableName: String, rowParser: RowParser[T], field: String = "id"): List[T] = {
    if (ids.length > 0) {
      DB.withConnection { implicit connection =>
        val (fields, values) = ids.zipWithIndex.map {
          case (id, i) => (s"{id$i}", NamedParameter(s"id$i", id))
        }.unzip
        val idFields = fields.mkString(", ")
        SQL(s"select * from $tableName where $field IN ($idFields)").on(values: _*).as(rowParser *)
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
      SQL(s"insert into $tableName($tableFields) values ($tableFieldsValues)").on(params: _*).executeInsert().map(id => id.toString)
    }
  }

  def update[T](id: String, values: List[(String, ParameterValue)], tableName: String, fieldId: String = "id"): Int = {
    DB.withConnection { implicit connection =>
      val fields = values.map(_._1)
      val sqlFields = fields.map(str => s"$tableName.$str={$str}").mkString(", ")
      val valuesWithId = values ++ List(("id", id: ParameterValue))
      val params = valuesWithId.map(t => NamedParameter(t._1, t._2))
      SQL(s"update $tableName set $sqlFields where $fieldId={id}").on(params: _*).executeUpdate()
    }
  }

  def delete(id: String, tableName: String, fieldId: String = "id"): Int = {
    DB.withConnection { implicit connection =>
      SQL(s"delete from $tableName where $fieldId={id}").on('id -> id).executeUpdate()
    }
  }
}

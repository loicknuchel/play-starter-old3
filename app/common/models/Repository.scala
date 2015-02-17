package common.models

/*
 * Convention :
 *  - methods get* return one result (Option[T])
 *  - methods find* return a list of results (List[T])
 */
trait Repository[A] {
  def findAll(filter: String = "", orderBy: String = ""): MonadicResult[List[A]]
  def findPage(page: Int = 1, filter: String = "", orderBy: String = ""): MonadicResult[Page[A]]
  def getByUuid(uuid: UUID): MonadicResult[Option[A]]
  def insert(elt: A): MonadicResult[Option[A]]
  def update(uuid: UUID, elt: A): MonadicResult[Option[A]]
  def delete(uuid: UUID): MonadicResult[Option[A]]
}
object Repository {
  def generateUuid(): UUID = java.util.UUID.randomUUID().toString()
}

package common.models

trait Repository[A] {
  def generateUuid(): UUID = java.util.UUID.randomUUID().toString()
  def findAll(filter: String = "", orderBy: String = ""): MonadicResult[List[A]]
  def findPage(page: Int = 1, filter: String = "", orderBy: String = ""): MonadicResult[Page[A]]
  def findByUuid(uuid: UUID): MonadicResult[Option[A]]
  def insert(elt: A): MonadicResult[Option[A]]
  def update(uuid: UUID, elt: A): MonadicResult[Option[A]]
  def delete(uuid: UUID): MonadicResult[Option[A]]
}

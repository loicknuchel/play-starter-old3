package common.models

trait Repository[A] {
  def generateUuid(): UUID = java.util.UUID.randomUUID().toString()
  def findAll(): MonadicResult[List[A]]
  def findPage(page: Int): MonadicResult[Page[A]]
  def findByUuid(uuid: UUID): MonadicResult[Option[A]]
  def insert(elt: A): MonadicResult[Option[A]]
  def update(uuid: UUID, elt: A): MonadicResult[Option[A]]
  def delete(uuid: UUID): MonadicResult[Option[A]]
}

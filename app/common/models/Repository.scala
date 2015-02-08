package common.models

trait Repository[A, ResultWrapper[_]] {
  def findAll(): ResultWrapper[List[A]]
  def findPage(page: Int): ResultWrapper[Page[A]]
  def findByUuid(uuid: UUID): ResultWrapper[Option[A]]
  def insert(elt: A): ResultWrapper[Option[A]]
  def update(uuid: UUID, elt: A): ResultWrapper[Option[A]]
  def delete(uuid: UUID): ResultWrapper[Option[A]]
}

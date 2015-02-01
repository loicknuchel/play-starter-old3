package common.models

import play.api.libs.json.Json

case class Page[A](items: Seq[A], currentPage: Int, pageSize: Int, totalItems: Long) {
  import java.math
  lazy val prev: Option[Int] = Option(currentPage - 1).filter(_ >= 1)
  lazy val next: Option[Int] = Option(currentPage + 1).filter(_ => ((currentPage - 1) * pageSize + items.size) < totalItems)
  lazy val totalPages: Int = new java.math.BigDecimal(totalItems).divide(new math.BigDecimal(pageSize)).setScale(0, BigDecimal.RoundingMode.UP).toInt
}
object Page {
  // TODO : implicit val format = Json.format[Page]
}

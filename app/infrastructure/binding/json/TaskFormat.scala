package infrastructure.binding.json

import domain.models.Task
import play.api.libs.json.Json

object TaskFormat {
  implicit val format = Json.format[Task]
}

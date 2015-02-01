package infrastructure.connection.mongodb

import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

trait Connection {
	val db = ReactiveMongoPlugin.db
	def collection: JSONCollection
}

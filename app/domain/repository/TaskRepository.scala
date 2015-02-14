package domain.repository

import infrastructure.connection.sql.SqlTaskRepository
import infrastructure.connection.mongodb.MongoDbTaskRepository

object TaskRepository extends MongoDbTaskRepository

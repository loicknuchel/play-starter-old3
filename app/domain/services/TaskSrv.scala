package domain.services

import domain.models.Task
import domain.models.TaskData
import domain.repository.TaskRepository

object TaskSrv {
  def create(data: TaskData): Option[Task] = {
    val task = TaskData.toModel(data)
    TaskRepository.insert(task)
  }
  def update(uuid: String, data: TaskData): Option[Task] = {
    val task = TaskData.toModel(data)
    TaskRepository.update(uuid, task)
  }
}
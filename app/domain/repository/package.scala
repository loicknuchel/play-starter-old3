package domain

import scala.concurrent.Future

package object repository {
  type SyncResult[X] = X
  type AsyncResult[X] = Future[X]

  type UUID = String
}

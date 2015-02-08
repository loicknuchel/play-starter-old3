package common

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Await
import scala.concurrent.duration._

package object models {
  type UUID = String

  def MonadicResult[A](a: Future[A]): MonadicResult[A] = AsyncResult(a)
  def MonadicResult[A](a: A): MonadicResult[A] = SyncResult(a)
  def AsyncResult[A](a: A): MonadicResult[A] = AsyncResult(Future.successful(a))
  
  sealed trait MonadicResult[A] {
    def get: Future[A]
    def getSync: A
    def map[B](f: (A) => B): MonadicResult[B]
    def flatMap[B](f: (A) ⇒ MonadicResult[B]): MonadicResult[B]
  }
  case class SyncResult[A](a: A) extends MonadicResult[A] {
    def get: Future[A] = Future.successful(a)
    def getSync: A = a
    def map[B](f: (A) => B): SyncResult[B] = SyncResult(f(a))
    def flatMap[B](f: (A) ⇒ MonadicResult[B]): MonadicResult[B] = f(a)
  }
  case class AsyncResult[A](a: Future[A]) extends MonadicResult[A] {
    def get: Future[A] = a
    def getSync: A = Await.result(a, 3.seconds)
    def map[B](f: (A) => B): AsyncResult[B] = AsyncResult(a.map(f(_)))
    def flatMap[B](f: (A) ⇒ MonadicResult[B]): MonadicResult[B] = AsyncResult(a.flatMap(u => f(u).get))
  }
}

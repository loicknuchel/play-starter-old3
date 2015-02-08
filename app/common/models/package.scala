package common

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

package object models {
  type UUID = String

  type SyncResult[X] = Sync[X]
  type AsyncResult[X] = Async[X]

  def MonadicResult[X](x: X): MonadicResult[X] = Sync(x)
  def SyncResult[X](x: X): SyncResult[X] = Sync(x)
  def AsyncResult[X](x: Future[X]): AsyncResult[X] = Async(x)
  def AsyncResult[X](x: X): AsyncResult[X] = Async(Future.successful(x))
  
  sealed trait MonadicResult[A] {
    def get: Future[A]
    def map[B](f: (A) => B): MonadicResult[B]
    def flatMap[B](f: (A) ⇒ MonadicResult[B]): MonadicResult[B]
  }
  case class Sync[A](a: A) extends MonadicResult[A] {
    def get: Future[A] = Future.successful(a)
    def getSync: A = a
    def map[B](f: (A) => B): Sync[B] = Sync(f(a))
    def flatMap[B](f: (A) ⇒ MonadicResult[B]): MonadicResult[B] = f(a)
  }
  case class Async[A](a: Future[A]) extends MonadicResult[A] {
    def get: Future[A] = a
    def map[B](f: (A) => B): Async[B] = Async(a.map(f(_)))
    def flatMap[B](f: (A) ⇒ MonadicResult[B]): MonadicResult[B] = Async(a.flatMap(u => f(u).get))
  }
}

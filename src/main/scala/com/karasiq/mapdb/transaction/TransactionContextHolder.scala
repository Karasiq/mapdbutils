package com.karasiq.mapdb.transaction

import scala.concurrent.Future

trait TransactionContextHolder {
  def doInTransaction[T](tx: TransactionContextHolder ⇒ T): Future[T]
}
package com.karasiq.mapdb.transaction

import scala.concurrent.Future

/**
  * Transaction context holder
  */
trait TxCtx {
  def doInTransaction[T](tx: TxCtx ⇒ T): Future[T]
}
package com.karasiq.mapdb.transaction

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

import com.karasiq.mapdb.MapDbProvider

import scala.util.control.Exception

trait TransactionHolder {
  def commit(): Unit
  def rollback(): Unit
}

final private class MapDbTransactionHolder(db: org.mapdb.DB) extends TransactionHolder {
  override def commit(): Unit = db.commit()

  override def rollback(): Unit = db.rollback()
}

trait TransactionProvider {
  private val transactionLock = new ReentrantLock(true)

  final protected def makeTransaction[T](tx: TransactionHolder)(action: ⇒ T): T = {
    if (transactionLock.tryLock(10, TimeUnit.MINUTES)) {
      Exception.allCatch.andFinally(transactionLock.unlock()) {
        if (transactionLock.getHoldCount > 1) {
          action
        } else {
          val catcher = Exception.allCatch.withApply { exc ⇒
            tx.rollback()
            throw exc
          }

          catcher {
            val result: T = action
            tx.commit()
            result
          }
        }
      }
    } else {
      throw new IOException("Couldn't acquire transaction lock")
    }
  }

  def withTransaction[T](action: ⇒ T): T
}

trait MapDbTransactionProvider extends TransactionProvider { self: MapDbProvider ⇒
  override final def withTransaction[T](action: ⇒ T): T = {
    makeTransaction(new MapDbTransactionHolder(db))(action)
  }
}

package com.karasiq.mapdb.transaction

import java.util.concurrent.atomic.AtomicReference

import com.karasiq.mapdb.MapDbProvider

import scala.util.control.Exception

trait TransactionProvider { self: MapDbProvider ⇒
  private val transaction = new AtomicReference[TransactionHolder](null)

  final def withTransaction[T](action: ⇒ T): T = db.synchronized {
    transaction.get() match {
      case null ⇒
        val tx: TransactionHolder = new TransactionHolder {
          override def rollback(): Unit = {
            if (transaction.compareAndSet(this, null)) {
              db.rollback()
            }
          }

          override def commit(): Unit = {
            if (transaction.compareAndSet(this, null)) {
              db.commit()
            }
          }
        }
        if (transaction.compareAndSet(null, tx)) {
          val catcher = Exception.allCatch.withApply { exc ⇒
            tx.rollback()
            throw exc
          }
          catcher {
            val result: T = action
            tx.commit()
            result
          }
        } else {
          withTransaction[T](action) // Retry
        }

      case tx: TransactionHolder ⇒
        val catcher = Exception.allCatch.withApply { exc ⇒
          tx.rollback()
          throw exc
        }
        catcher {
          val result: T = action
          // No commit
          result
        }
    }
  }
}

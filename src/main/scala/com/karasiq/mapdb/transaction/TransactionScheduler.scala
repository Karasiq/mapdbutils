package com.karasiq.mapdb.transaction

import java.util.concurrent.Executors

import com.karasiq.mapdb.MapDbProvider

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

trait TransactionScheduler { self: MapDbProvider ⇒
  protected final val txSchedulerExecutionContext = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  private object NoTransaction extends TransactionContextHolder {
    override def doInTransaction[T](tx: TransactionContextHolder ⇒ T): Future[T] = {
      implicit def context: ExecutionContext = TransactionScheduler.this.txSchedulerExecutionContext

      val newContext = new TransactionContextHolder {
        override def doInTransaction[T1](tx: (TransactionContextHolder) ⇒ T1): Future[T1] = {
          Future.fromTry(Try(tx(this)))
        }
      }

      Future {
        val result = try {
          tx(newContext)
        } catch {
          case th: Throwable ⇒
            db.rollback()
            throw th
        }

        // No errors
        db.commit()
        result
      }
    }
  }

  def newTransaction: TransactionContextHolder = NoTransaction

  /**
    * Performs asynchronous transaction
    * @param tx Transaction body
    * @param ctx Transaction context
    * @tparam T Result type
    * @return Future
    */
  final def scheduleTransaction[T](tx: TransactionContextHolder ⇒ T)(implicit ctx: TransactionContextHolder = newTransaction): Future[T] = {
    ctx.doInTransaction[T](tx)
  }

  /**
    * Performs synchronous transaction
    * @param tx Transaction body
    * @param ctx Transaction context
    * @tparam T Result type
    * @return Transaction result
    */
  final def withTransaction[T](tx: TransactionContextHolder ⇒ T)(implicit ctx: TransactionContextHolder = newTransaction): T = {
    val future = scheduleTransaction(tx)(ctx)
    Await.result(future, Duration.Inf)
  }
}

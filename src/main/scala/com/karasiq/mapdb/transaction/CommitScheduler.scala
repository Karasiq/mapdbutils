package com.karasiq.mapdb.transaction

import java.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.{Timer, TimerTask}

import com.karasiq.mapdb.MapDbProvider
import com.typesafe.config.ConfigFactory
import org.mapdb.DB

trait CommitSchedulerProvider { self: MapDbProvider ⇒
  @transient
  final lazy val commitScheduler: CommitScheduler = CommitScheduler(db)
}

/**
 * Asynchronous commit scheduler
 */
abstract sealed class CommitScheduler extends Closeable {
  private lazy val commitTask = new AtomicReference[Option[TimerTask]](None)

  protected def makeCommit(): Unit

  private def scheduleCommit(): Unit = {
    val newTask = new TimerTask {
      override def run(): Unit = {
        if (commitTask.compareAndSet(Some(this), None)) {
          makeCommit()
        }
      }
    }

    if (commitTask.compareAndSet(None, Some(newTask))) {
      CommitScheduler.timer.schedule(newTask, CommitScheduler.interval)
    }
  }

  final def commit(instant: Boolean = false): Unit = {
    if (instant || CommitScheduler.interval == 0) makeCommit()
    else scheduleCommit()
  }

  override def close(): Unit = {
    commitTask.getAndSet(None).foreach { task ⇒
      task.cancel()
      makeCommit()
    }
  }
}

object CommitScheduler {
  def apply(db: DB): CommitScheduler = new CommitScheduler {
    override protected def makeCommit(): Unit = {
      assert(!db.isClosed, "DB already closed")
      db.commit()
    }
  }

  /**
   * Commit interval in milliseconds
   * If == 0, always commit synchronous
   */
  private[mapdb] val interval: Long = {
    val cfg = ConfigFactory.load().getConfig("mapDb.commitScheduler")
    cfg.getDuration("interval", TimeUnit.MILLISECONDS)
  }

  private[mapdb] lazy val timer = new Timer("CommitScheduler", false)
}

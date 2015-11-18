package com.karasiq.mapdb

import java.io.Closeable
import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import com.karasiq.mapdb.transaction.TransactionScheduler
import org.mapdb.DBMaker.Maker
import org.mapdb._

import scala.collection.concurrent.TrieMap

/**
 * MapDB file wrapper
 */
sealed abstract class MapDbFile extends MapDbProvider with TransactionScheduler with Closeable {
  def path: Path

  override def toString: String = s"MapDbFile($path)"

  override def hashCode(): Int = {
    path.hashCode()
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case f: MapDbFile ⇒
      path.equals(f.path)

    case _ ⇒
      false
  }

  override def close(): Unit = {
    txSchedulerExecutionContext.shutdown()
    txSchedulerExecutionContext.awaitTermination(5, TimeUnit.MINUTES)
    if (!db.isClosed) db.close()
  }
}

object MapDbFile {
  def apply(database: DB, dbPath: Path = null): MapDbFile = new MapDbFile {
    override def db: DB = database

    override def path: Path = dbPath
  }
}

/**
 * MapDB file wrapper factory
 */
abstract class MapDbFileProducer extends Closeable {
  protected def setSettings(dbMaker: DBMaker.Maker): DBMaker.Maker

  private def createDb(f: Path): MapDbFile = {
    Files.createDirectories(f.getParent)
    MapDbFile(setSettings(DBMaker.fileDB(f.toFile)).make(), f)
  }

  private val dbMap = TrieMap.empty[String, MapDbFile]

  final def apply(f: Path): MapDbFile = synchronized {
    dbMap.filter(_._2.db.isClosed).foreach(dbMap -= _._1) // Remove closed
    dbMap.getOrElseUpdate(f.toString, createDb(f))
  }

  private def close(k: String): Unit = synchronized {
    dbMap.remove(k).foreach(_.close())
  }

  final def close(f: Path): Unit = synchronized {
    close(f.toString)
  }

  override def close(): Unit = synchronized {
    dbMap.keys.foreach(close)
  }
}

abstract class MapDbSingleFileProducer(path: Path) extends Closeable {
  assert(!Files.exists(path) || Files.isRegularFile(path), s"Not a file: $path")

  protected def setSettings(dbMaker: DBMaker.Maker): DBMaker.Maker

  private val producer = MapDbFileProducer(setSettings)

  def apply(): MapDbFile = producer(path)

  override def close(): Unit = producer.close()
}


object MapDbFileProducer {
  def apply(f: DBMaker.Maker ⇒ DBMaker.Maker): MapDbFileProducer = new MapDbFileProducer {
    override protected def setSettings(dbMaker: Maker): Maker = f(dbMaker)
  }
}
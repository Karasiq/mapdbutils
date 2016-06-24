package com.karasiq.mapdb

import java.io.{Closeable, File}
import java.nio.file.{Files, Path, Paths}
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
    override val db: DB = database
    override val path: Path = dbPath
  }

  def producer(settings: DBMaker.Maker ⇒ DBMaker.Maker): MapDbFileProducer = new MapDbFileProducer {
    override protected def setSettings(dbMaker: Maker): Maker = settings(dbMaker)
  }

  def apply(path: Path)(settings: DBMaker.Maker ⇒ DBMaker.Maker): MapDbSingleFileProducer = new MapDbSingleFileProducer(path) {
    protected def setSettings(dbMaker: Maker) = settings(dbMaker)
  }

  def apply(path: String)(settings: DBMaker.Maker ⇒ DBMaker.Maker): MapDbSingleFileProducer = apply(Paths.get(path))(settings)
  def apply(path: File)(settings: DBMaker.Maker ⇒ DBMaker.Maker): MapDbSingleFileProducer = apply(path.toPath)(settings)
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

  final def apply(path: Path): MapDbFile = synchronized {
    dbMap.filter(_._2.db.isClosed).foreach(dbMap -= _._1) // Remove closed
    dbMap.getOrElseUpdate(path.toString, createDb(path))
  }

  final def apply(path: String): MapDbFile = apply(Paths.get(path))

  final def apply(path: File): MapDbFile = apply(path.toPath)

  private def close(path: String): Unit = synchronized {
    dbMap.remove(path).foreach(_.close())
  }

  final def close(path: Path): Unit = synchronized {
    close(path.toString)
  }

  override def close(): Unit = synchronized {
    dbMap.keys.foreach(close)
  }
}

abstract class MapDbSingleFileProducer(final val path: Path) extends Closeable {
  assert(!Files.exists(path) || Files.isRegularFile(path), s"Not a file: $path")

  protected def setSettings(dbMaker: DBMaker.Maker): DBMaker.Maker

  private val producer = MapDbFile.producer(setSettings)

  def apply(): MapDbFile = producer(path)

  override def close(): Unit = producer.close()
}
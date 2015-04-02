package com.karasiq.mapdb

import java.io.Closeable
import java.nio.file.{Files, Path, Paths}

import org.mapdb._

import scala.collection.concurrent.TrieMap

sealed abstract class MapDbFile extends Closeable {
  def db: DB
  def path: Path

  override def toString: String = s"MapDbFile($path)"

  override def close(): Unit = {
    commitScheduler.close()
    if (!db.isClosed) db.close()
  }

  @transient
  lazy val commitScheduler: CommitScheduler = CommitScheduler(db)
}

object MapDbFile {
  def apply(database: DB, path: Path = null): MapDbFile = new MapDbFile {
    override def db: DB = database

    override def path: Path = path
  }
}

abstract class MapDbFileProducer {
  protected def setSettings[T <: DBMaker[T]](dbMaker: DBMaker[T]): T

  private def createDb(f: Path): MapDbFile = {
    Files.createDirectories(f.getParent)
    MapDbFile(setSettings(DBMaker.newFileDB(f.toFile)).make(), f)
  }

  private val dbMap = {
    val map = TrieMap.empty[String, MapDbFile]
    map.withDefault(key ⇒ {
      val db = createDb(Paths.get(key))
      map.putIfAbsent(key, db) match {
        case Some(existingDb) ⇒ // Already opened
          db.close()
          existingDb
        case None ⇒
          db
      }
    })
  }

  final def apply(f: Path): MapDbFile = {
    dbMap.filter(_._2.db.isClosed).foreach(dbMap -= _._1) // Remove closed
    dbMap(f.toString)
  }

  private def close(k: String): Unit = {
    dbMap.remove(k).foreach(_.close())
  }

  final def close(f: Path): Unit = close(f.toString)

  final def close(): Unit = dbMap.keys.foreach(close)
}

package com.karasiq.mapdb

import java.io.Closeable
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicReference

import com.karasiq.mapdb.transaction.TransactionHolder
import org.mapdb._

import scala.collection.JavaConversions._
import scala.collection.concurrent.TrieMap
import scala.collection.{Set, mutable}
import scala.util.control.Exception

sealed class MapDbWrappedMap[K, V, M <: java.util.Map[K, V]](mapDbMap: M) extends mutable.AbstractMap[K, V] {
  def underlying(): M = mapDbMap
  
  override def +=(kv: (K, V)): this.type = {
    mapDbMap.put(kv._1, kv._2)
    this
  }

  override def -=(key: K): this.type = {
    mapDbMap.remove(key)
    this
  }

  override def get(key: K): Option[V] = {
    if (mapDbMap.containsKey(key)) {
      Some(mapDbMap.get(key))
    } else {
      None
    }
  }

  override def iterator: Iterator[(K, V)] = {
    keysIterator.collect {
      case key if mapDbMap.containsKey(key) ⇒
        key → mapDbMap.get(key)
    }
  }

  override def valuesIterator: Iterator[V] = {
    mapDbMap.values().toIterator
  }

  override def keysIterator: Iterator[K] = {
    mapDbMap.keySet().toIterator
  }

  override def keySet: Set[K] = {
    mapDbMap.keySet()
  }
}

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

  private val transaction = new AtomicReference[TransactionHolder](null)

  final def withTransaction[T](action: ⇒ T): T = {
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
  
  def wrappedMap[K, V, M <: java.util.Map[K, V]](createMap: DB ⇒ M): MapDbWrappedMap[K, V, M] = {
    val mapDbMap = createMap(db)
    new MapDbWrappedMap(mapDbMap)
  }
  
  def hashMap[K, V](name: String): MapDbWrappedMap[K, V, HTreeMap[K, V]] = {
    this.wrappedMap(_.hashMap(name))
  }
}

object MapDbFile {
  def apply(database: DB, dbPath: Path = null): MapDbFile = new MapDbFile {
    override def db: DB = database

    override def path: Path = dbPath
  }
}

abstract class MapDbFileProducer {
  protected def setSettings(dbMaker: DBMaker.Maker): DBMaker.Maker

  private def createDb(f: Path): MapDbFile = {
    Files.createDirectories(f.getParent)
    MapDbFile(setSettings(DBMaker.fileDB(f.toFile)).make(), f)
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

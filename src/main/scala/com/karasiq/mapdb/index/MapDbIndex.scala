package com.karasiq.mapdb.index

import com.karasiq.mapdb.MapDbConversions._
import org.mapdb.{Bind, DB}

import scala.collection.JavaConversions._
import scala.collection.immutable.AbstractMap

object MapDbIndex {
  type JavaMap[K, V] = java.util.Map[K, V]

  object IndexMaps {
    def heapHashMap[K, V](): JavaMap[K, V] = {
      new java.util.concurrent.ConcurrentHashMap[K, V]()
    }

    def heapTreeMap[K, V](): JavaMap[K, V] = {
      new java.util.concurrent.ConcurrentSkipListMap[K, V]()
    }

    def mapDbHashMap[K, V](db: DB, name: String): JavaMap[K, V] = {
      db.hashMap(name)
    }

    def mapDbTreeMap[K, V](db: DB, name: String): JavaMap[K, V] = {
      db.treeMap(name)
    }
  }
  
  private final class WrappedIndexMap[K, V](secondaryMap: JavaMap[K, V]) extends AbstractMap[K, V] {
    override def get(key: K): Option[V] = {
      Option(secondaryMap.get(key))
    }

    override def iterator: Iterator[(K, V)] = {
      secondaryMap.keySet().toIterator.collect {
        case key if secondaryMap.containsKey(key) ⇒
          key → secondaryMap.get(key)
      }
    }

    private def exception(): Nothing = throw new IllegalArgumentException("Couldn't modify index map")

    override def +[B1 >: V](kv: (K, B1)): Map[K, B1] = exception()

    override def -(key: K): Map[K, V] = exception()
  }

  def secondaryKey[K, V, SK](map: Bind.MapWithModificationListener[K, V], function: (K, V) ⇒ SK, secondaryMap: JavaMap[SK, K] = IndexMaps.heapHashMap()): Map[SK, K] = {
    Bind.secondaryKey(map, secondaryMap, function)
    new WrappedIndexMap(secondaryMap)
  }

  def secondaryValue[K, V, SV](map: Bind.MapWithModificationListener[K, V], function: (K, V) ⇒ SV, secondaryMap: JavaMap[K, SV] = IndexMaps.heapHashMap()): Map[K, SV] = {
    Bind.secondaryValue(map, secondaryMap, function)
    new WrappedIndexMap(secondaryMap)
  }
}

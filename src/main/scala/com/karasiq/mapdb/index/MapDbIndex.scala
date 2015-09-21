package com.karasiq.mapdb.index

import org.mapdb.Bind
import org.mapdb.Fun.Function2

import scala.collection.JavaConversions._
import scala.collection.immutable.AbstractMap

object MapDbIndex {
  object Map {
    def heapHashMap[K, V](): java.util.Map[K, V] = new java.util.HashMap[K, V]()
    def heapTreeMap[K, V](): java.util.Map[K, V] = new java.util.TreeMap[K, V]()
  }
  
  private sealed class WrappedIndexMap[K, V](secondaryMap: java.util.Map[K, V]) extends AbstractMap[K, V] {
    override def get(key: K): Option[V] = {
      if (secondaryMap.containsKey(key)) {
        Some(secondaryMap.get(key))
      } else {
        None
      }
    }

    override def iterator: Iterator[(K, V)] = {
      secondaryMap.keySet().toIterator.collect {
        case key if secondaryMap.containsKey(key) ⇒
          key → secondaryMap.get(key)
      }
    }

    override def +[B1 >: V](kv: (K, B1)): Map[K, B1] = {
      throw new IllegalArgumentException
    }

    override def -(key: K): Map[K, V] = {
      throw new IllegalArgumentException
    }
  }

  def secondaryKey[K, V, SK](map: Bind.MapWithModificationListener[K, V], f: (K, V) ⇒ SK, secondaryMap: java.util.Map[SK, K] = Map.heapHashMap()): Map[SK, K] = {
    Bind.secondaryKey(map, secondaryMap, new Function2[SK, K, V] {
      override def run(a: K, b: V): SK = {
        f(a, b)
      }
    })

    new WrappedIndexMap(secondaryMap)
  }
}

package com.karasiq.mapdb

import org.mapdb.{BTreeMap, DB, HTreeMap}

import scala.collection.JavaConversions._

object MapDbWrapper {
  type MapDbHashMap[K, V] = MapDbWrappedMap[K, V, HTreeMap[K, V]]
  type MapDbTreeMap[K, V] = MapDbWrappedMap[K, V, BTreeMap[K, V]]
  type MapDbHashSet[V] = MapDbWrappedSet[V, java.util.Set[V]]
  type MapDbTreeSet[V] = MapDbWrappedSet[V, java.util.NavigableSet[V]]

  def apply(database: DB): MapDbWrapper = new MapDbWrapper {
    override protected def db: DB = database
  }

  def apply(file: MapDbFile): MapDbWrapper = new MapDbWrapper {
    override protected def db: DB = file.db
  }
}

sealed class MapDbWrappedMap[K, V, M <: java.util.Map[K, V]](mapDbMap: M) extends scala.collection.mutable.AbstractMap[K, V] {
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
    Option(mapDbMap.get(key))
  }

  override def iterator: Iterator[(K, V)] = {
    mapDbMap.toIterator
  }

  override def valuesIterator: Iterator[V] = {
    mapDbMap.values().toIterator
  }

  override def keysIterator: Iterator[K] = {
    mapDbMap.keySet().toIterator
  }

  override def keySet: Set[K] = {
    mapDbMap.keySet().toSet
  }
}

sealed class MapDbWrappedSet[V, S <: java.util.Set[V]](mapDbSet: S) extends scala.collection.mutable.AbstractSet[V] {
  def underlying(): S = mapDbSet

  override def +=(elem: V): MapDbWrappedSet.this.type = {
    mapDbSet.add(elem)
    this
  }

  override def -=(elem: V): MapDbWrappedSet.this.type = {
    mapDbSet.remove(elem)
    this
  }

  override def contains(elem: V): Boolean = {
    mapDbSet.contains(elem)
  }

  override def iterator: Iterator[V] = {
    mapDbSet.iterator().toIterator
  }
}

sealed abstract class MapDbWrapper {
  import MapDbWrapper._

  protected def db: DB

  private def wrappedMap[K, V, M <: java.util.Map[K, V]](createMap: DB ⇒ M): MapDbWrappedMap[K, V, M] = {
    val mapDbMap = createMap(db)
    new MapDbWrappedMap(mapDbMap)
  }

  private def wrappedSet[V, S <: java.util.Set[V]](createSet: DB ⇒ S): MapDbWrappedSet[V, S] = {
    val mapDbSet = createSet(db)
    new MapDbWrappedSet(mapDbSet)
  }

  def createHashMap[K, V](createMap: DB ⇒ HTreeMap[K, V]): MapDbHashMap[K, V] = {
    wrappedMap(createMap)
  }

  def createTreeMap[K, V](createMap: DB ⇒ BTreeMap[K, V]): MapDbTreeMap[K, V] = {
    wrappedMap(createMap)
  }

  def hashMap[K, V](name: String): MapDbHashMap[K, V] = {
    wrappedMap(_.hashMap(name))
  }

  def treeMap[K, V](name: String): MapDbTreeMap[K, V] = {
    wrappedMap(_.treeMap(name))
  }

  def createTreeSet[V](createSet: DB ⇒ java.util.NavigableSet[V]): MapDbTreeSet[V] = {
    wrappedSet(createSet)
  }

  def createHashSet[V](createSet: DB ⇒ java.util.Set[V]): MapDbHashSet[V] = {
    wrappedSet(createSet)
  }

  def treeSet[V](name: String): MapDbTreeSet[V] = {
    wrappedSet(_.treeSet(name))
  }

  def hashSet[V](name: String): MapDbHashSet[V] = {
    wrappedSet(_.hashSet(name))
  }
}
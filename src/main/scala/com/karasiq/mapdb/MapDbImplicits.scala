package com.karasiq.mapdb

import org.mapdb.serializer.GroupSerializer
import org.mapdb.{BTreeMap, DB, HTreeMap, Serializer}

object MapDbImplicits {
  type MapDbHashMap[K, V] = HTreeMap[K, V]
  type MapDbTreeMap[K, V] = BTreeMap[K, V]
  type MapDbHashSet[V] = java.util.Set[V]
  type MapDbTreeSet[V] = java.util.NavigableSet[V]

  sealed trait ScalaMapDbWrapper {
    def db: DB

    def createHashMap[K: Serializer, V: Serializer](name: String)(createMap: DB.HashMapMaker[K, V] ⇒ DB.HashMapMaker[K, V]): MapDbHashMap[K, V] = {
      createMap(db.hashMap(name, implicitly[Serializer[K]], implicitly[Serializer[V]])).createOrOpen()
    }

    def createTreeMap[K: GroupSerializer, V: GroupSerializer](name: String)(createMap: DB.TreeMapMaker[K, V] ⇒ DB.TreeMapMaker[K, V]): MapDbTreeMap[K, V] = {
      createMap(db.treeMap(name, implicitly[GroupSerializer[K]], implicitly[GroupSerializer[V]])).createOrOpen()
    }

    def hashMap[K: Serializer, V: Serializer](name: String): MapDbHashMap[K, V] = {
      createHashMap(name)(identity)
    }

    def treeMap[K: GroupSerializer, V: GroupSerializer](name: String): MapDbTreeMap[K, V] = {
      createTreeMap(name)(identity)
    }

    def createTreeSet[V: GroupSerializer](name: String)(createSet: DB.TreeSetMaker[V] ⇒ DB.TreeSetMaker[V]): MapDbTreeSet[V] = {
      createSet(db.treeSet[V](name, implicitly[GroupSerializer[V]])).createOrOpen()
    }

    def createHashSet[V: Serializer](name: String)(createSet: DB.HashSetMaker[V] ⇒ DB.HashSetMaker[V]): MapDbHashSet[V] = {
      createSet(db.hashSet[V](name, implicitly[Serializer[V]])).createOrOpen()
    }

    def treeSet[V: GroupSerializer](name: String): MapDbTreeSet[V] = {
      createTreeSet(name)(identity)
    }

    def hashSet[V: Serializer](name: String): MapDbHashSet[V] = {
      createHashSet(name)(identity)
    }
  }

  implicit class MapDbOps(private val mapDb: DB) extends AnyVal {
    def scala: ScalaMapDbWrapper = new ScalaMapDbWrapper {
      def db = mapDb
    }
  }

  implicit class MapDbFileOps(mapDbFile: MapDbFile) extends ScalaMapDbWrapper  {
    def db = mapDbFile.db
  }
}

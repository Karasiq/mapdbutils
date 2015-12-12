# About
Scala wrappers for MapDB

# How to use
## SBT configuration
In your `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "com.github.karasiq" %% "mapdbutils" % "1.1.0",
  "org.mapdb" % "mapdb" % "2.0-beta12"
)
```

## Basic DB and map
```scala
import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.MapDbWrapper._

// Create DB
val mapDb = MapDbFile(DBMaker.memoryDB().transactionDisable().make())

// Create basic hash map
val map = mapDb.hashMap[String, String]("test")

// Create custom hash map
val map1 = mapDb.createHashMap[String, String]("test")(_
  .keySerializer(MapDbSerializer[String])
  .valueSerializer(MapDbSerializer[String])
)
```

## Serializers
```scala
import com.karasiq.mapdb.serialization.MapDbSerializer, MapDbSerializer.Default._

case class Test1(strs: Seq[(String, String)], longs: Array[Long])
case class Test2(tt: Vector[Test1])

// Uses compile-time macro
val serializer = MapDbSerializer[Test2]
```


## DB file producer
```scala
object TestDbProducer extends MapDbFileProducer {
  override protected def setSettings(dbMaker: Maker): Maker = {
    // Example settings
    dbMaker
      .compressionEnable()
      .executorEnable()
      .cacheSoftRefEnable()
  }
}

object TestSingleDbProducer extends MapDbSingleFileProducer(Paths.get("test1.db")) {
  override protected def setSettings(dbMaker: Maker): Maker = {
    // Example settings
    dbMaker
      .compressionEnable()
      .executorEnable()
      .cacheSoftRefEnable()
  }
}

// Opens DB
val db = TestDbProducer(Paths.get("test.db"))
val db1 = TestSingleDbProducer()

// Closes previously opened DBs
TestDbProducer.close()
TestSingleDbProducer.close()
```

## Transactions
* Asynchronous
```scala
val result: Future[String] = mapDb.scheduleTransaction { implicit tx ⇒
  // Rollbacks and returns unsuccessful future on exception
  map.put("key", "value")
  "result"
}
```
* Synchronous
```scala
val result: String = mapDb.withTransaction { implicit tx ⇒
  // Same as above, but synchronous
  map.put("key", "value")
  "result"
}
```

## Indexes
```scala
import com.karasiq.mapdb.index.MapDbIndex
import com.karasiq.mapdb.index.MapDbIndex.IndexMaps

val mapDb = MapDbFile(DBMaker.memoryDB().transactionDisable().make())
val map = mapDb.hashMap[String, String]("test")
val index = MapDbIndex.secondaryKey[String, String, Int](map.underlying(), (k, v) ⇒ v.hashCode(), IndexMaps.mapDbHashMap(mapDb.db, "test_index")) // Uses hash code as key

map.put("key1", "value1")
index.get("value1".hashCode) // -> Some("key1")
```
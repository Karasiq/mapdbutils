# mapdbutils [![Build Status](https://travis-ci.org/Karasiq/mapdbutils.svg?branch=master)](https://travis-ci.org/Karasiq/mapdbutils)
Scala wrappers for MapDB

# How to use
## SBT configuration
In your `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "com.github.karasiq" %% "mapdbutils" % "1.1.1",
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

# License
The MIT License (MIT)

Copyright (c) 2016 Karasiq

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

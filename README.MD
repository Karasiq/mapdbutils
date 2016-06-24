# mapdbutils [![Build Status](https://travis-ci.org/Karasiq/mapdbutils.svg?branch=master)](https://travis-ci.org/Karasiq/mapdbutils) [![Dependencies](https://app.updateimpact.com/badge/692686982173822976/mapdbutils.svg?config=compile)](https://app.updateimpact.com/latest/692686982173822976/mapdbutils)
Scala wrappers for MapDB

# How to use
## SBT configuration
In your `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "com.github.karasiq" %% "mapdbutils" % "1.2.0-SNAPSHOT",
  "org.mapdb" % "mapdb" % "3.0.0-RC2"
)
```

## Basic DB and map
```scala
import org.mapdb.DBMaker
import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.MapDbImplicits._
import com.karasiq.mapdb.serialization.MapDbSerializer

// Create DB
val mapDb = DBMaker.memoryDB().make().scala

// Create map
val map = mapDb.hashMap[String, String]("test")

// Create custom map
val map1 = mapDb.createHashMap[String, String]("test")(_
  .counterEnable()
  .expireAfterCreate(10000)
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
import com.karasiq.mapdb.MapDbFile
val producer = MapDbFile.producer(_
  .executorEnable()
  .fileMmapEnable()
)

val single = MapDbFile("test1.db")(_
  .executorEnable()
  .fileMmapEnable()
)

// Opens DB
val db = producer("test.db")
val db1 = single()

// Closes previously opened DBs
producer.close()
single.close()
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

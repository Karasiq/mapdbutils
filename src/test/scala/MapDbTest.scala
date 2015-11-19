import java.time.Instant

import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.MapDbWrapper._
import com.karasiq.mapdb.index.MapDbIndex
import com.karasiq.mapdb.index.MapDbIndex.IndexMaps
import com.karasiq.mapdb.serialization.MapDbSerializer
import com.karasiq.mapdb.serialization.MapDbSerializer.Default._
import eu.timepit.refined.numeric.Positive
import org.mapdb.{DBMaker, Serializer}
import org.scalatest.{FlatSpec, Matchers}
import shapeless.tag.@@

case class TestCaseClass(str: String, int: Int, times: Seq[Instant])

class MapDbTest extends FlatSpec with Matchers {
  "MapDB" should "commit" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().make())
    val map = mapDb.hashMap[String, String]("test")
    mapDb.withTransaction { implicit tx ⇒
      map.put("key1", "value1")
      mapDb.withTransaction { implicit tx ⇒
        map.put("key2", "value2")
      }
    }
    map.get("key1") shouldBe Some("value1")
    map.get("key2") shouldBe Some("value2")
    mapDb.close()
  }

  it should "rollback" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().make())
    val map = mapDb.createHashMap[String, String]("test") { _
      .keySerializer(MapDbSerializer[String])
      .valueSerializer(MapDbSerializer[String])
    }
    intercept[IllegalArgumentException] {
      mapDb.withTransaction { implicit tx ⇒
        map += ("key1" → "value1")
        mapDb.withTransaction { implicit tx ⇒
          map += ("key2" → "value2")
        }
        throw new IllegalArgumentException
      }
    }
    map.get("key1") shouldBe None
    map.get("key2") shouldBe None
    mapDb.close()
  }

  it should "create secondary key" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().make())
    val map = mapDb.hashMap[String, String]("test")
    val index = MapDbIndex.secondaryKey[String, String, Int](map.underlying(), (k, v) ⇒ v.hashCode(), IndexMaps.mapDbHashMap(mapDb.db, "test_index"))

    mapDb.withTransaction { implicit tx ⇒
      map.put("key1", "value1")
      mapDb.withTransaction { implicit tx ⇒
        map.put("key2", "value2")
      }
    }
    index.get("value1".hashCode) shouldBe Some("key1")
    index.get("value2".hashCode) shouldBe Some("key2")
    mapDb.close()
  }

  it should "create secondary value" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().make())
    val map = mapDb.hashMap[String, String]("test")
    val index = MapDbIndex.secondaryValue[String, String, Int](map.underlying(), (k, v) ⇒ v.hashCode(), IndexMaps.mapDbHashMap(mapDb.db, "test_hashes"))

    mapDb.withTransaction { implicit tx ⇒
      map.put("key1", "value1")
      mapDb.withTransaction { implicit tx ⇒
        map.put("key2", "value2")
      }
    }
    index.get("key1") shouldBe Some("value1".hashCode)
    index.get("key2") shouldBe Some("value2".hashCode)
    mapDb.close()
  }

  "Serializer" should "be implicitly created" in {
    import MapDbSerializer.Default._

    assert(MapDbSerializer[Long].eq(Serializer.LONG.asInstanceOf[Serializer[Long]]))
    assert(MapDbSerializer[Long @@ Positive].eq(Serializer.LONG_PACKED.asInstanceOf[Serializer[Long]]))
    assert(MapDbSerializer[Array[Byte]].eq(Serializer.BYTE_ARRAY))
    val serializer3 = MapDbSerializer[TestCaseClass]
    val serializer4 = MapDbSerializer[Vector[Int]]
    val serializer5 = MapDbSerializer[Array[String]]
    val serializer7 = MapDbSerializer[Map[Char, String]]
    val serializer8 = MapDbSerializer[(String, Long)]
    val serializer9 = MapDbSerializer[Set[Array[String]]]
  }
}

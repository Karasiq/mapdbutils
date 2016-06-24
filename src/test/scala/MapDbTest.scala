import java.time.Instant

import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.MapDbImplicits._
import com.karasiq.mapdb.serialization.MapDbSerializer
import com.karasiq.mapdb.serialization.MapDbSerializer.Default._
import eu.timepit.refined.numeric.Positive
import org.mapdb.{DBMaker, Serializer}
import org.scalatest.{FlatSpec, Matchers}
import shapeless.tag.@@

import scala.collection.JavaConverters._

case class TestCaseClass(str: String, int: Int, times: Seq[Instant])

class MapDbTest extends FlatSpec with Matchers {
  "MapDB" should "commit" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().transactionEnable().make())
    val map = mapDb.hashMap[String, String]("test").asScala
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

  // TODO: org.mapdb.DBException$GetVoid: Record does not exist, recid=9
  ignore should "rollback" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().transactionEnable().make())
    val map = mapDb.hashMap[String, String]("test").asScala
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

  // TODO: java.lang.NoClassDefFoundError: org/mapdb/org.mapdb.
  ignore should "create hash set" in {
    val mapDb = MapDbFile(DBMaker.memoryDB().transactionEnable().make())
    val set = mapDb.hashSet[String]("test").asScala

    mapDb.withTransaction { implicit tx ⇒
      set += "test value"
    }

    set should contain ("test value")
  }

  "Serializer" should "be implicitly created" in {
    import MapDbSerializer.Default._

    assert(MapDbSerializer[Long].eq(Serializer.LONG_DELTA.asInstanceOf[Serializer[Long]]))
    assert(MapDbSerializer[Long @@ Positive].eq(Serializer.LONG_PACKED.asInstanceOf[Serializer[Long]]))
    assert(MapDbSerializer[Array[Byte]].eq(Serializer.BYTE_ARRAY_DELTA))
    MapDbSerializer[Byte]
    MapDbSerializer[Vector[Int]]
    MapDbSerializer[Array[String]]
    MapDbSerializer[Map[Char, String]]
    MapDbSerializer[Set[Array[String]]]
    MapDbSerializer[TestCaseClass]
    MapDbSerializer[(String, Long)]
  }
}

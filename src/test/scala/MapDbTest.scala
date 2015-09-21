import com.karasiq.mapdb.MapDbFile
import com.karasiq.mapdb.index.MapDbIndex
import org.mapdb.DBMaker
import org.scalatest.{FlatSpec, Matchers}

class MapDbTest extends FlatSpec with Matchers {
  "MapDB" should "commit" in {
    val mapDb = MapDbFile(DBMaker.heapDB().make())
    val map = mapDb.hashMap[String, String]("test")
    mapDb.withTransaction {
      map.put("key1", "value1")
      mapDb.withTransaction {
        map.put("key2", "value2")
      }
    }
    map.get("key1") shouldBe Some("value1")
    map.get("key2") shouldBe Some("value2")
    mapDb.close()
  }

  it should "rollback" in {
    val mapDb = MapDbFile(DBMaker.heapDB().make())
    val map = mapDb.hashMap[String, String]("test")
    intercept[IllegalArgumentException] {
      mapDb.withTransaction {
        map.put("key1", "value1")
        mapDb.withTransaction {
          map.put("key2", "value2")
        }
        throw new IllegalArgumentException
      }
    }
    map.get("key1") shouldBe None
    map.get("key2") shouldBe None
    mapDb.close()
  }

  it should "create index" in {
    val mapDb = MapDbFile(DBMaker.heapDB().make())
    val map = mapDb.hashMap[String, String]("test")
    val index = MapDbIndex.secondaryKey[String, String, Int](map.underlying(), (k, v) ⇒ v.hashCode(), MapDbIndex.Map.heapTreeMap())

    mapDb.withTransaction {
      map.put("key1", "value1")
      mapDb.withTransaction {
        map.put("key2", "value2")
      }
    }
    index.get("value1".hashCode) shouldBe Some("key1")
    index.get("value2".hashCode) shouldBe Some("key2")
    mapDb.close()
  }
}

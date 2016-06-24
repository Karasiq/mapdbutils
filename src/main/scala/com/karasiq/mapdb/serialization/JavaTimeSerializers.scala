package com.karasiq.mapdb.serialization

import java.time._

import org.mapdb.serializer.{GroupSerializer, GroupSerializerObjectArray}
import org.mapdb.{DataInput2, DataOutput2}

trait JavaTimeSerializers { self: PredefinedSerializers ⇒
  implicit def instantSerializer: GroupSerializer[Instant] = new GroupSerializerObjectArray[Instant] {
    override def serialize(out: DataOutput2, value: Instant): Unit = {
      out.packLong(value.getEpochSecond)
      out.packInt(value.getNano)
    }

    override def deserialize(in: DataInput2, available: Int): Instant = {
      Instant.ofEpochSecond(in.unpackLong(), in.unpackInt())
    }
  }

  implicit def zonedDateTimeSerializer: GroupSerializer[ZonedDateTime] = javaObjectSerializer[ZonedDateTime]

  implicit def localDateTimeSerializer: GroupSerializer[LocalDateTime] = javaObjectSerializer[LocalDateTime]

  implicit def localDateSerializer: GroupSerializer[LocalDate] = javaObjectSerializer[LocalDate]

  implicit def localTimeSerializer: GroupSerializer[LocalTime] = javaObjectSerializer[LocalTime]
}

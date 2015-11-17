package com.karasiq.mapdb.serialization

import java.io.{DataInput, DataOutput}
import java.time._

import org.mapdb.{DataIO, Serializer}

trait JavaTimeSerializers { self: PredefinedSerializers ⇒
  implicit def instantSerializer: Serializer[Instant] = new Serializer[Instant] {
    override def serialize(out: DataOutput, value: Instant): Unit = {
      DataIO.packLong(out, value.getEpochSecond)
      DataIO.packInt(out, value.getNano)
    }

    override def deserialize(in: DataInput, available: Int): Instant = {
      Instant.ofEpochSecond(DataIO.unpackLong(in), DataIO.unpackInt(in))
    }
  }

  implicit def zonedDateTimeSerializer: Serializer[ZonedDateTime] = javaObjectSerializer[ZonedDateTime]

  implicit def localDateTimeSerializer: Serializer[LocalDateTime] = javaObjectSerializer[LocalDateTime]

  implicit def localDateSerializer: Serializer[LocalDate] = javaObjectSerializer[LocalDate]

  implicit def localTimeSerializer: Serializer[LocalTime] = javaObjectSerializer[LocalTime]
}

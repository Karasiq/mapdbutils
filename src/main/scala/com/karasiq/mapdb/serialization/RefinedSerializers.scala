package com.karasiq.mapdb.serialization

import eu.timepit.refined.numeric.Positive
import org.mapdb.Serializer
import org.mapdb.serializer.GroupSerializer
import shapeless.tag.@@

/**
  * @see [[https://github.com/fthomas/refined]]
  */
trait RefinedSerializers {
  implicit def positiveIntSerializer: GroupSerializer[Int @@ Positive] = Serializer.INTEGER_PACKED.asInstanceOf[GroupSerializer[Int @@ Positive]]
  implicit def positiveLongSerializer: GroupSerializer[Long @@ Positive] = Serializer.LONG_PACKED.asInstanceOf[GroupSerializer[Long @@ Positive]]
}

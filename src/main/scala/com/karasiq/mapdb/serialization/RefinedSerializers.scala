package com.karasiq.mapdb.serialization

import eu.timepit.refined.numeric.Positive
import org.mapdb.Serializer
import shapeless.tag.@@

/**
  * @see [[https://github.com/fthomas/refined]]
  */
trait RefinedSerializers {
  implicit val positiveIntSerializer: Serializer[Int @@ Positive] = Serializer.INTEGER_PACKED.asInstanceOf[Serializer[Int @@ Positive]]

  implicit val positiveLongSerializer: Serializer[Long @@ Positive] = Serializer.LONG_PACKED.asInstanceOf[Serializer[Long @@ Positive]]
}

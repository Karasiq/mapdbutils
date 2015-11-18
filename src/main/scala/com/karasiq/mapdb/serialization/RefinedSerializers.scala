package com.karasiq.mapdb.serialization

import eu.timepit.refined.numeric.{Negative, Positive}
import org.mapdb.Serializer
import shapeless.tag.@@

/**
  * @see [[https://github.com/fthomas/refined]]
  */
trait RefinedSerializers {
  implicit val positiveIntSerializer: Serializer[Int @@ Positive] = Serializer.INTEGER_PACKED.asInstanceOf[Serializer[Int @@ Positive]]

  implicit val positiveLongSerializer: Serializer[Long @@ Positive] = Serializer.LONG_PACKED.asInstanceOf[Serializer[Long @@ Positive]]

  implicit val negativeIntSerializer: Serializer[Int @@ Negative] = Serializer.INTEGER_PACKED_ZIGZAG.asInstanceOf[Serializer[Int @@ Negative]]

  implicit val negativeLongSerializer: Serializer[Long @@ Negative] = Serializer.LONG_PACKED_ZIGZAG.asInstanceOf[Serializer[Long @@ Negative]]
}

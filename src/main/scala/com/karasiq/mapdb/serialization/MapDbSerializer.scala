package com.karasiq.mapdb.serialization

import org.mapdb.Serializer

object MapDbSerializer {
  /**
    * @see [[Default.serializableSerializer]]
    */
  def java[T <: Serializable]: Serializer[T] = Default.serializableSerializer[T]

  def apply[T](implicit sr: Serializer[T]): Serializer[T] = {
    sr
  }

  def orDefault[T](implicit sr: Serializer[T] = java[T]): Serializer[T] = {
    sr
  }

  /**
   * Default implicit serializers container
   */
  object Default extends PredefinedSerializers with JavaTimeSerializers with ScalaSerializers with MacroSerializers
}

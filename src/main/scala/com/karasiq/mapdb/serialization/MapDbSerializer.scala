package com.karasiq.mapdb.serialization

import org.mapdb.Serializer
import org.mapdb.serializer.GroupSerializer

import scala.annotation.implicitNotFound

object MapDbSerializer {
  /**
    * @see [[Default.javaObjectSerializer]]
    */
  def java[T]: Serializer[T] = Default.javaObjectSerializer[T]

  @implicitNotFound("GroupSerializer not found for type ${T}")
  def apply[T](implicit sr: GroupSerializer[T]): GroupSerializer[T] = {
    sr
  }

  @implicitNotFound("Serializer not found for type ${T}")
  def plain[T](implicit sr: Serializer[T]): Serializer[T] = {
    sr
  }

  def getOrDefault[T](implicit sr: Serializer[T] = java[T]): Serializer[T] = {
    sr
  }

  /**
   * Default implicit serializers container
   */
  object Default extends PredefinedSerializers with JavaTimeSerializers with ScalaSerializers with MacroSerializers with RefinedSerializers
}

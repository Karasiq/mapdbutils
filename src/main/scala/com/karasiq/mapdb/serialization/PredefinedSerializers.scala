package com.karasiq.mapdb.serialization

import org.mapdb.Serializer
import org.mapdb.serializer.GroupSerializer

trait PredefinedSerializers {
  /**
    * Serializer which uses standard Java Serialization with [[java.io.ObjectInputStream]] and [[java.io.ObjectOutputStream]]
    * @tparam T Object type
    * @return Serializer
    */
  def javaObjectSerializer[T]: GroupSerializer[T] = Serializer.JAVA.asInstanceOf[GroupSerializer[T]]

  /**
    * elsa - Java serialization, faster and space efficient version of ObjectOutputStream
    * @tparam T Object type
    * @return Serializer
    */
  def elsaSerializer[T]: GroupSerializer[T] = Serializer.ELSA.asInstanceOf[GroupSerializer[T]]

  // Primitives
  implicit def stringSerializer: GroupSerializer[String] = Serializer.STRING_DELTA
  implicit def intSerializer: GroupSerializer[Int] = Serializer.INTEGER_DELTA.asInstanceOf[GroupSerializer[Int]]
  implicit def longSerializer: GroupSerializer[Long] = Serializer.LONG_DELTA.asInstanceOf[GroupSerializer[Long]]
  implicit def shortSerializer: GroupSerializer[Short] = Serializer.SHORT.asInstanceOf[GroupSerializer[Short]]
  implicit def byteSerializer: GroupSerializer[Byte] = Serializer.BYTE.asInstanceOf[GroupSerializer[Byte]]
  implicit def charSerializer: GroupSerializer[Char] = Serializer.CHAR.asInstanceOf[GroupSerializer[Char]]
  implicit def doubleSerializer: GroupSerializer[Double] = Serializer.DOUBLE.asInstanceOf[GroupSerializer[Double]]
  implicit def floatSerializer: GroupSerializer[Float] = Serializer.FLOAT.asInstanceOf[GroupSerializer[Float]]
  implicit def booleanSerializer: GroupSerializer[Boolean] = Serializer.BOOLEAN.asInstanceOf[GroupSerializer[Boolean]]
  implicit def javaUuidSerializer: GroupSerializer[java.util.UUID] = Serializer.UUID
  implicit def javaBigIntegerSerializer: GroupSerializer[java.math.BigInteger] = Serializer.BIG_INTEGER
  implicit def javaBigDecimalSerializer: GroupSerializer[java.math.BigDecimal] = Serializer.BIG_DECIMAL
  implicit def javaDateSerializer: GroupSerializer[java.util.Date] = Serializer.DATE

  // Arrays
  implicit def intArraySerializer: GroupSerializer[Array[Int]] = Serializer.INT_ARRAY
  implicit def longArraySerializer: GroupSerializer[Array[Long]] = Serializer.LONG_ARRAY
  implicit def shortArraySerializer: GroupSerializer[Array[Short]] = Serializer.SHORT_ARRAY
  implicit def byteArraySerializer: GroupSerializer[Array[Byte]] = Serializer.BYTE_ARRAY_DELTA
  implicit def charArraySerializer: GroupSerializer[Array[Char]] = Serializer.CHAR_ARRAY
  implicit def doubleArraySerializer: GroupSerializer[Array[Double]] = Serializer.DOUBLE_ARRAY
  implicit def floatArraySerializer: GroupSerializer[Array[Float]] = Serializer.FLOAT_ARRAY
}

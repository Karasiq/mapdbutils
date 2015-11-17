package com.karasiq.mapdb.serialization

import org.mapdb.Serializer

trait PredefinedSerializers {
  /**
    * Standard Java Serialization wrapper
    * @tparam T Object type
    * @return Serializer
    */
  def serializableSerializer[T <: Serializable]: Serializer[T] = Serializer.JAVA.asInstanceOf[Serializer[T]]

  // Primitives
  implicit def stringSerializer: Serializer[String] = Serializer.STRING_XXHASH

  implicit def intSerializer: Serializer[Int] = Serializer.INTEGER.asInstanceOf[Serializer[Int]]

  implicit def longSerializer: Serializer[Long] = Serializer.LONG.asInstanceOf[Serializer[Long]]

  implicit def shortSerializer: Serializer[Short] = Serializer.SHORT.asInstanceOf[Serializer[Short]]

  implicit def byteSerializer: Serializer[Byte] = Serializer.BYTE.asInstanceOf[Serializer[Byte]]

  implicit def charSerializer: Serializer[Char] = Serializer.CHAR.asInstanceOf[Serializer[Char]]

  implicit def doubleSerializer: Serializer[Double] = Serializer.DOUBLE.asInstanceOf[Serializer[Double]]

  implicit def floatSerializer: Serializer[Float] = Serializer.FLOAT.asInstanceOf[Serializer[Float]]

  implicit def booleanSerializer: Serializer[Boolean] = Serializer.BOOLEAN.asInstanceOf[Serializer[Boolean]]

  implicit def javaUuidSerializer: Serializer[java.util.UUID] = Serializer.UUID

  implicit def javaBigIntegerSerializer: Serializer[java.math.BigInteger] = Serializer.BIG_INTEGER

  implicit def javaBigDecimalSerializer: Serializer[java.math.BigDecimal] = Serializer.BIG_DECIMAL

  implicit def javaDateSerializer: Serializer[java.util.Date] = Serializer.DATE

  // Arrays
  implicit def intArraySerializer: Serializer[Array[Int]] = Serializer.INT_ARRAY

  implicit def longArraySerializer: Serializer[Array[Long]] = Serializer.LONG_ARRAY

  implicit def shortArraySerializer: Serializer[Array[Short]] = Serializer.SHORT_ARRAY

  implicit def byteArraySerializer: Serializer[Array[Byte]] = Serializer.BYTE_ARRAY

  implicit def charArraySerializer: Serializer[Array[Char]] = Serializer.CHAR_ARRAY

  implicit def doubleArraySerializer: Serializer[Array[Double]] = Serializer.DOUBLE_ARRAY

  implicit def floatArraySerializer: Serializer[Array[Float]] = Serializer.FLOAT_ARRAY

  implicit def booleanArraySerializer: Serializer[Array[Boolean]] = Serializer.BOOLEAN_ARRAY
}

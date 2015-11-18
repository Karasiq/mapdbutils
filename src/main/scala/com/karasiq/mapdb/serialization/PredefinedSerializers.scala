package com.karasiq.mapdb.serialization

import org.mapdb.Serializer

trait PredefinedSerializers {
  /**
    * Standard Java Serialization wrapper
    * @tparam T Object type
    * @return Serializer
    */
  def javaObjectSerializer[T]: Serializer[T] = Serializer.JAVA.asInstanceOf[Serializer[T]]

  // Primitives
  implicit val stringSerializer: Serializer[String] = Serializer.STRING_XXHASH

  implicit val intSerializer: Serializer[Int] = Serializer.INTEGER.asInstanceOf[Serializer[Int]]

  implicit val longSerializer: Serializer[Long] = Serializer.LONG.asInstanceOf[Serializer[Long]]

  implicit val shortSerializer: Serializer[Short] = Serializer.SHORT.asInstanceOf[Serializer[Short]]

  implicit val byteSerializer: Serializer[Byte] = Serializer.BYTE.asInstanceOf[Serializer[Byte]]

  implicit val charSerializer: Serializer[Char] = Serializer.CHAR.asInstanceOf[Serializer[Char]]

  implicit val doubleSerializer: Serializer[Double] = Serializer.DOUBLE.asInstanceOf[Serializer[Double]]

  implicit val floatSerializer: Serializer[Float] = Serializer.FLOAT.asInstanceOf[Serializer[Float]]

  implicit val booleanSerializer: Serializer[Boolean] = Serializer.BOOLEAN.asInstanceOf[Serializer[Boolean]]

  implicit val javaUuidSerializer: Serializer[java.util.UUID] = Serializer.UUID

  implicit val javaBigIntegerSerializer: Serializer[java.math.BigInteger] = Serializer.BIG_INTEGER

  implicit val javaBigDecimalSerializer: Serializer[java.math.BigDecimal] = Serializer.BIG_DECIMAL

  implicit val javaDateSerializer: Serializer[java.util.Date] = Serializer.DATE

  // Arrays
  implicit val intArraySerializer: Serializer[Array[Int]] = Serializer.INT_ARRAY

  implicit val longArraySerializer: Serializer[Array[Long]] = Serializer.LONG_ARRAY

  implicit val shortArraySerializer: Serializer[Array[Short]] = Serializer.SHORT_ARRAY

  implicit val byteArraySerializer: Serializer[Array[Byte]] = Serializer.BYTE_ARRAY

  implicit val charArraySerializer: Serializer[Array[Char]] = Serializer.CHAR_ARRAY

  implicit val doubleArraySerializer: Serializer[Array[Double]] = Serializer.DOUBLE_ARRAY

  implicit val floatArraySerializer: Serializer[Array[Float]] = Serializer.FLOAT_ARRAY

  implicit val booleanArraySerializer: Serializer[Array[Boolean]] = Serializer.BOOLEAN_ARRAY
}

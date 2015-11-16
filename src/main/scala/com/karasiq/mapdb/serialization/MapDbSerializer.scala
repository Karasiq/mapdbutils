package com.karasiq.mapdb.serialization

import java.io.{DataInput, DataOutput}

import com.karasiq.mapdb.MapDbMacro
import org.mapdb.{DataIO, Serializer}

import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.ClassTag

object MapDbSerializer {
  def apply[T](implicit sr: Serializer[T]): Serializer[T] = {
    sr
  }

  def orDefault[T](implicit sr: Serializer[T] = Serializer.JAVA): Serializer[T] = {
    sr
  }

  sealed trait GenericSerializer {
    implicit def materializeSerializer[T]: Serializer[T] = macro MapDbMacro.materializeSerializerImpl[T]
  }

  /**
   * Default implicit serializers
   */
  object Default extends GenericSerializer {
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


    // Custom
    implicit def bigDecimalSerializer: Serializer[BigDecimal] = new Serializer[BigDecimal] {
      override def serialize(out: DataOutput, value: BigDecimal): Unit = {
        javaBigDecimalSerializer.serialize(out, value.underlying())
      }

      override def deserialize(in: DataInput, available: Int): BigDecimal = {
        BigDecimal(javaBigDecimalSerializer.deserialize(in, available))
      }
    }

    implicit def bigIntSerializer: Serializer[BigInt] = new Serializer[BigInt] {
      override def serialize(out: DataOutput, value: BigInt): Unit = {
        javaBigIntegerSerializer.serialize(out, value.underlying())
      }

      override def deserialize(in: DataInput, available: Int): BigInt = {
        BigInt(javaBigIntegerSerializer.deserialize(in, available))
      }
    }

    private def collectionSerializer[T : Serializer, C[`T`] <: Traversable[T]](f: Iterator[T] ⇒ C[T]): Serializer[C[T]] = new Serializer[C[T]] {
      override def serialize(out: DataOutput, value: C[T]): Unit = {
        require(value.hasDefiniteSize, "Undefined size")
        DataIO.packInt(out, value.size)
        value.foreach(orDefault[T].serialize(out, _))
      }

      override def deserialize(in: DataInput, available: Int): C[T] = {
        val length = DataIO.unpackInt(in)
        val buffer = (0 until length).toIterator.map(_ ⇒ orDefault[T].deserialize(in, available))
        f(buffer)
      }
    }

    implicit def arraySerializer[T : ClassTag](implicit sr: Serializer[T]): Serializer[Array[T]] = new Serializer[Array[T]] {
      override def serialize(out: DataOutput, value: Array[T]): Unit = {
        require(value.hasDefiniteSize, "Undefined size")
        DataIO.packInt(out, value.length)
        value.indices.foreach { i ⇒
          sr.serialize(out, value(i))
        }
      }

      override def deserialize(in: DataInput, available: Int): Array[T] = {
        val length = DataIO.unpackInt(in)
        val array = new Array[T](length)
        (0 until length).foreach { i ⇒
          array.update(i, sr.deserialize(in, available))
        }
        array
      }
    }

    implicit def seqSerializer[T: Serializer]: Serializer[Seq[T]] = collectionSerializer(_.toSeq)

    implicit def vectorSerializer[T: Serializer]: Serializer[Vector[T]] = collectionSerializer(_.toVector)

    implicit def listSerializer[T: Serializer]: Serializer[List[T]] = collectionSerializer(_.toList)

    implicit def iterableSerializer[T: Serializer]: Serializer[Iterable[T]] = collectionSerializer(_.toIterable)

    implicit def traversableSerializer[T: Serializer]: Serializer[Traversable[T]] = collectionSerializer(_.toTraversable)

    implicit def mapSerializer[K : Serializer, V : Serializer]: Serializer[Map[K, V]] = {
      new Serializer[Map[K, V]] {
        override def serialize(out: DataOutput, value: Map[K, V]): Unit = {
          DataIO.packInt(out, value.size)
          value.iterator.foreach { case (k, v) ⇒
            orDefault[K].serialize(out, k)
            orDefault[V].serialize(out, v)
          }
        }

        override def deserialize(in: DataInput, available: Int): Map[K, V] = {
          val length = DataIO.unpackInt(in)
          assert(length >= 0)
          val entries = (0 until length).toIterator.map { _ ⇒
            orDefault[K].deserialize(in, available) → orDefault[V].deserialize(in, available)
          }
          entries.toMap
        }
      }
    }

    implicit def optionSerializer[T: Serializer]: Serializer[Option[T]] = new Serializer[Option[T]] {
      override def serialize(out: DataOutput, value: Option[T]): Unit = {
        out.writeBoolean(value.isDefined)
        value.foreach(implicitly[Serializer[T]].serialize(out, _))
      }

      override def deserialize(in: DataInput, available: Int): Option[T] = {
        if (in.readBoolean()) {
          Some(implicitly[Serializer[T]].deserialize(in, available))
        } else {
          None
        }
      }
    }
  }
}

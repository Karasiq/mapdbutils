package com.karasiq.mapdb.serialization

import java.util.concurrent.TimeUnit

import org.mapdb.serializer.{GroupSerializer, GroupSerializerObjectArray}
import org.mapdb.{DataInput2, DataOutput2, Serializer}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.higherKinds
import scala.reflect.ClassTag

trait ScalaSerializers { self: PredefinedSerializers ⇒
  implicit def bigDecimalSerializer: GroupSerializer[BigDecimal] = new GroupSerializerObjectArray[BigDecimal] {
    override def serialize(out: DataOutput2, value: BigDecimal): Unit = {
      javaBigDecimalSerializer.serialize(out, value.underlying())
    }

    override def deserialize(in: DataInput2, available: Int): BigDecimal = {
      BigDecimal(javaBigDecimalSerializer.deserialize(in, available))
    }
  }

  implicit def bigIntSerializer: GroupSerializer[BigInt] = new GroupSerializerObjectArray[BigInt] {
    override def serialize(out: DataOutput2, value: BigInt): Unit = {
      javaBigIntegerSerializer.serialize(out, value.underlying())
    }

    override def deserialize(in: DataInput2, available: Int): BigInt = {
      BigInt(javaBigIntegerSerializer.deserialize(in, available))
    }
  }

  implicit def durationSerializer: GroupSerializer[Duration] = new GroupSerializerObjectArray[Duration] {
    override def serialize(out: DataOutput2, value: Duration): Unit = {
      if (value.isFinite()) {
        out.writeBoolean(true)
        out.packLong(value.toMillis)
      } else {
        out.writeBoolean(false)
      }
    }

    override def deserialize(in: DataInput2, available: Int): Duration = {
      if (in.readBoolean()) {
        FiniteDuration(in.unpackLong(), TimeUnit.MILLISECONDS)
      } else {
        Duration.Inf
      }
    }
  }

  // Collections
  def scalaCollectionSerializer[T: Serializer, C[`T`] <: Traversable[T]](f: (Int, Iterator[T]) ⇒ C[T]): GroupSerializer[C[T]] = new GroupSerializerObjectArray[C[T]] {
    override def serialize(out: DataOutput2, value: C[T]): Unit = {
      require(value.hasDefiniteSize, "Undefined size")
      out.packInt(value.size)
      value.foreach(implicitly[Serializer[T]].serialize(out, _))
    }

    override def deserialize(in: DataInput2, available: Int): C[T] = {
      val length = in.unpackInt()
      assert(length >= 0, "Negative length")
      val buffer = (0 until length).toIterator.map(_ ⇒ implicitly[Serializer[T]].deserialize(in, available))
      f(length, buffer)
    }
  }

  implicit def arraySerializer[T <: AnyRef : ClassTag](implicit sr: Serializer[T]): GroupSerializer[Array[T]] = new GroupSerializerObjectArray[Array[T]] {
    override def serialize(out: DataOutput2, value: Array[T]): Unit = {
      out.packInt(value.length)
      value.indices.foreach { i ⇒
        sr.serialize(out, value(i))
      }
    }

    override def deserialize(in: DataInput2, available: Int): Array[T] = {
      val length = in.unpackInt()
      val array = new Array[T](length)
      (0 until length).foreach { i ⇒
        array.update(i, sr.deserialize(in, available))
      }
      array
    }
  }

  private def fromBuilder[T, C](b: scala.collection.mutable.Builder[T, C])(length: Int, iterator: Iterator[T]): C = {
    b.sizeHint(length)
    iterator.foreach { value ⇒
      b += value
    }
    b.result()
  }

  @inline
  private def createVector[T](length: Int, iterator: Iterator[T]): Vector[T] = fromBuilder(Vector.newBuilder[T])(length, iterator)

  @inline
  private def createList[T](length: Int, iterator: Iterator[T]): List[T] = fromBuilder(new ListBuffer[T])(length, iterator)

  @inline
  private def createSet[T](length: Int, iterator: Iterator[T]): Set[T] = fromBuilder(Set.newBuilder[T])(length, iterator)

  //noinspection MutatorLikeMethodIsParameterless
  implicit def setSerializer[T: Serializer]: GroupSerializer[Set[T]] = scalaCollectionSerializer(createSet)

  implicit def seqSerializer[T: Serializer]: GroupSerializer[Seq[T]] = scalaCollectionSerializer(createList)

  implicit def vectorSerializer[T: Serializer]: GroupSerializer[Vector[T]] = scalaCollectionSerializer(createVector)

  implicit def listSerializer[T: Serializer]: GroupSerializer[List[T]] = scalaCollectionSerializer(createList)

  implicit def iterableSerializer[T: Serializer]: GroupSerializer[Iterable[T]] = scalaCollectionSerializer(createList)

  implicit def traversableSerializer[T: Serializer]: GroupSerializer[Traversable[T]] = scalaCollectionSerializer(createList)

  implicit def mapSerializer[K: Serializer, V: Serializer]: GroupSerializer[Map[K, V]] = {
    new GroupSerializerObjectArray[Map[K, V]] {
      override def serialize(out: DataOutput2, value: Map[K, V]): Unit = {
        out.packInt(value.size)
        value.iterator.foreach { case (k, v) ⇒
          implicitly[Serializer[K]].serialize(out, k)
          implicitly[Serializer[V]].serialize(out, v)
        }
      }

      override def deserialize(in: DataInput2, available: Int): Map[K, V] = {
        val length = in.unpackInt()
        assert(length >= 0, "Negative length")
        val entries = (0 until length).toIterator.map { _ ⇒
          implicitly[Serializer[K]].deserialize(in, available) → implicitly[Serializer[V]].deserialize(in, available)
        }
        entries.toMap
      }
    }
  }

  implicit def optionSerializer[T: Serializer]: GroupSerializer[Option[T]] = new GroupSerializerObjectArray[Option[T]] {
    override def serialize(out: DataOutput2, value: Option[T]): Unit = {
      out.writeBoolean(value.isDefined)
      value.foreach(implicitly[Serializer[T]].serialize(out, _))
    }

    override def deserialize(in: DataInput2, available: Int): Option[T] = {
      if (in.readBoolean()) {
        Some(implicitly[Serializer[T]].deserialize(in, available))
      } else {
        None
      }
    }
  }
}

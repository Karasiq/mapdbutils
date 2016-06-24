package com.karasiq.mapdb.serialization

import com.karasiq.mapdb.MapDbMacro
import org.mapdb.serializer.GroupSerializer

import scala.language.experimental.macros

trait MacroSerializers {
  implicit def materializeSerializer[T]: GroupSerializer[T] = macro MapDbMacro.materializeSerializerImpl[T]
  implicit def tupleSerializer[T]: GroupSerializer[T] = macro MapDbMacro.tupleSerializerImpl[T]
}

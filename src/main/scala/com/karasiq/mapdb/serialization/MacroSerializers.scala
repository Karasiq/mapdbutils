package com.karasiq.mapdb.serialization

import com.karasiq.mapdb.MapDbMacro
import org.mapdb.Serializer

import scala.language.experimental.macros

trait MacroSerializers {
  implicit def materializeSerializer[T]: Serializer[T] = macro MapDbMacro.materializeSerializerImpl[T]
}

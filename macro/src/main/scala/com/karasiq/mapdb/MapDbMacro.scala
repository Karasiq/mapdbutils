package com.karasiq.mapdb

import org.mapdb.Serializer

object MapDbMacro {
  def materializeSerializerImpl[T: c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context): c.Expr[Serializer[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor ⇒
        m.paramLists.head
    }

    if (fields.isEmpty || fields.exists(_.isEmpty)) {
      c.abort(c.enclosingPosition, "Object contains no fields")
    }

    val (serialize, deserialize) = fields.get.map { field ⇒
      if (!tpe.decl(field.name).isMethod || !tpe.decl(field.name).isPublic) {
        c.abort(c.enclosingPosition, s"No method found: ${field.name}")
      }

      val serializer = q"implicitly[org.mapdb.Serializer[${field.typeSignature}]]"
      (q"$serializer.serialize(out, value.${field.asTerm.name})", q"$serializer.deserialize(in, available)")
    }.unzip

    c.Expr[Serializer[T]] { q"""
        new org.mapdb.Serializer[$tpe] {
          override def serialize(out: java.io.DataOutput, value: $tpe): Unit = { ..$serialize }
          override def deserialize(in: java.io.DataInput, available: Int): $tpe = new $tpe(..$deserialize)
        }
      """ }
  }
}

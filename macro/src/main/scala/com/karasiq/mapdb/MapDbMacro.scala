package com.karasiq.mapdb

import org.mapdb.Serializer

object MapDbMacro {
  private def compileSerializer[T: c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context)(tpe: c.universe.Type, fields: List[(c.universe.TermName, c.universe.Type)]): c.Expr[Serializer[T]] = {
    import c.universe._

    val serializerType = tq"_root_.org.mapdb.Serializer"

    val (serialize, deserialize) = fields.map { case (fname, ftype) ⇒
      if (!tpe.decl(fname).isMethod || !tpe.decl(fname).isPublic) {
        c.abort(c.enclosingPosition, s"No method found: $fname")
      }

      if (!tpe.decl(fname).typeSignature.<:<(ftype)) {
        c.abort(c.enclosingPosition, s"Type not match: $fname $ftype")
      }

      val serializer = q"implicitly[$serializerType[$ftype]]"
      (q"$serializer.serialize(out, value.$fname)", q"$serializer.deserialize(in, available)")
    }.unzip

    c.Expr[Serializer[T]] { q"""
        new $serializerType[$tpe] {
          override def serialize(out: java.io.DataOutput, value: $tpe): Unit = { ..$serialize }
          override def deserialize(in: java.io.DataInput, available: Int): $tpe = new $tpe(..$deserialize)
        }
      """ }
  }

  def tupleSerializerImpl[T: c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context): c.Expr[Serializer[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor ⇒
        m.paramLists.head
    }
    val TypeRef(_, _, types) = tpe

    if (fields.isEmpty || fields.exists(_.isEmpty) || types.length != fields.get.length) {
      c.abort(c.enclosingPosition, "Not a tuple")
    }

    compileSerializer(c)(tpe, fields.get.map(_.asTerm.name).zip(types)).asInstanceOf[c.Expr[Serializer[T]]]
  }

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

    compileSerializer[T](c)(tpe, fields.get.map(f ⇒ f.asTerm.name → f.typeSignature)).asInstanceOf[c.Expr[Serializer[T]]]
  }
}

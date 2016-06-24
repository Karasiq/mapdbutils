package com.karasiq.mapdb

import org.mapdb.serializer.GroupSerializer

object MapDbMacro {
  private def compileSerializer[T: c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context)(tpe: c.universe.Type, fields: List[(c.universe.TermName, c.universe.Type)]): c.Expr[GroupSerializer[T]] = {
    import c.universe._

    val serializerType = tq"_root_.org.mapdb.Serializer"
    val groupSerializerType = tq"_root_.org.mapdb.serializer.GroupSerializerObjectArray"

    val (serialize, deserialize) = fields.map { case (fname, ftype) ⇒
      if (!tpe.decl(fname).isMethod || !tpe.decl(fname).isPublic) {
        c.abort(c.enclosingPosition, s"No method found: $fname")
      }

      val serializer = q"implicitly[$serializerType[$ftype]]"
      (q"$serializer.serialize(out, value.$fname)", q"$serializer.deserialize(in, available)")
    }.unzip

    c.Expr[GroupSerializer[T]] { q"""
        new $groupSerializerType[$tpe] {
          override def serialize(out: _root_.org.mapdb.DataOutput2, value: $tpe): Unit = { ..$serialize }
          override def deserialize(in: _root_.org.mapdb.DataInput2, available: Int): $tpe = new $tpe(..$deserialize)
        }
      """ }
  }

  def tupleSerializerImpl[T: c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context): c.Expr[GroupSerializer[T]] = {
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

    compileSerializer(c)(tpe, fields.get.map(_.asTerm.name).zip(types)).asInstanceOf[c.Expr[GroupSerializer[T]]]
  }

  def materializeSerializerImpl[T: c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context): c.Expr[GroupSerializer[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor ⇒
        m.paramLists.head
    }

    if (fields.isEmpty || fields.exists(_.isEmpty)) {
      c.abort(c.enclosingPosition, "Object contains no fields")
    }

    compileSerializer[T](c)(tpe, fields.get.map(f ⇒ f.asTerm.name → f.typeSignature)).asInstanceOf[c.Expr[GroupSerializer[T]]]
  }
}

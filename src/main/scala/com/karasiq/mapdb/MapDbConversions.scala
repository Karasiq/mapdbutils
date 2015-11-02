package com.karasiq.mapdb

import org.mapdb.Fun

import scala.language.implicitConversions

object MapDbConversions {
  implicit def mapDbFunction0[R](f: ⇒ R): Fun.Function0[R] = new Fun.Function0[R] {
    override def run(): R = f
  }

  implicit def mapDbFunction1[T1, R](f: T1 ⇒ R): Fun.Function1[R, T1] = new Fun.Function1[R, T1] {
    override def run(a: T1): R = {
      f(a)
    }
  }

  implicit def mapDbFunction1Int[R](f: Int ⇒ R): Fun.Function1Int[R] = new Fun.Function1Int[R] {
    override def run(i: Int): R = {
      f(i)
    }
  }

  implicit def mapDbFunction2[T1, T2, R](f: (T1, T2) ⇒ R): Fun.Function2[R, T1, T2] = new Fun.Function2[R, T1, T2] {
    override def run(a: T1, b: T2): R = {
      f(a, b)
    }
  }
}

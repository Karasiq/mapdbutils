package com.karasiq.mapdb

trait MapDbProvider {
  def db: org.mapdb.DB
}

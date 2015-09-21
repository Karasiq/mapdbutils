package com.karasiq.mapdb.transaction

trait TransactionHolder {
  def commit(): Unit
  def rollback(): Unit
}
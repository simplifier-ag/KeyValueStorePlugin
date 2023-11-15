package io.simplifier.plugin.keyValueStore.model

import io.simplifier.plugin.keyValueStore.pluginBaseRelated.data.Checksum
import io.simplifier.plugin.keyValueStore.pluginBaseRelated.db.GenericDao
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.squeryl.{KeyedEntity, Table}

import java.nio.charset.StandardCharsets

case class KeyValueStoreValue(@Column("id") id: String,
                              @Column("key_text") key: String,
                              @Column("data") data: Array[Byte]) extends KeyedEntity[String]

object KeyValueStoreValue {

  def apply(key: String, data: Array[Byte]): KeyValueStoreValue = {
    val id: String = encodeKey(key)
    KeyValueStoreValue(id, key, data)
  }

  def encodeKey(key: String): String = {
    val item = key.getBytes(StandardCharsets.UTF_8)
    Checksum.checksumSHA256(item)
  }

}

class KeyValueStoreValueDao extends GenericDao[KeyValueStoreValue, String] {

  override def table: Table[KeyValueStoreValue] = PluginSchema.valueT

  /**
    * Get list of all keys present in database
    */
  //noinspection AccessorLikeMethodIsEmptyParen
  def getAllKeys(): Seq[String] = inTransaction {
    from(table)(value => select(value.key)).toVector
  }

  /**
    * Find value via key (id is calculated from key and searched)
    *
    * @param key key of entity
    */
  def findByHashedKey(key: String): Option[KeyValueStoreValue] = {
    val id: String = KeyValueStoreValue.encodeKey(key)
    getById(id)
  }

  /**
    * Delete key from database (id is calculated from key and searched)
    *
    * @param key key of entity
    */
  def deleteByHashedKey(key: String): Unit = {
    val id: String = KeyValueStoreValue.encodeKey(key)
    delete(id)
  }

  /**
    * Check if given key exists in the database without selecting large fields (id is calculated from key and searched)
    *
    * @param key key of entity
    */
  def checkHashedKeyExists(key: String): Boolean = inTransaction {
    val id: String = KeyValueStoreValue.encodeKey(key)
    from(table)(v => where(v.id === id) select v.id).headOptionFixed.isDefined
  }

}
package io.simplifier.plugin.keyValueStore.helper

import com.typesafe.config.Config
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend.StoreType
import org.mapdb.{DB, DBMaker, HTreeMap, Serializer}

import java.io.File

/**
  * Abstract store backend defining interface for different store backend implementations.
  * Also contains logic to initialize mapdb file storage
  */
abstract class AbstractStoreBackend(config: Config, val storeType: StoreType) {

  /**
    * filename of storage file from config used by MapDb Backend or migration to database
    */
  lazy val mapDbFileName: String = config.getString("plugin.filename")

  /**
    * Get data from store for given key
    */
  def get(key: String): Option[Array[Byte]]

  /**
    * Update data in store for key
    *
    * @param key           key
    * @param value         updated value (either explicit value, or from "copyFrom" other key, or from AppServer asset)
    */
  def put(key: String, value: Array[Byte]): Unit

  /**
    * Check if the key exists
    */
  def exists(key: String): Boolean

  /**
    * Remove key from store
    */
  def delete(key: String): Unit

  /**
    * Get all keys that are present in store
    */
  def list: Seq[String]

  /**
    * Migrate file store to database
    */
  def migrateToDatabase(): Unit

  /**
    * Initialize store
    */
  def init(): Unit

  /**
    * Shutdown connected store
    */
  def shutdown(): Unit = {}

  /**
    * Init MapDB, either as Storage used in [[MapDbStoreBackend]], or during migration to DB.
    *
    * @return opened MapDB instance
    */
  protected def initMapDB(): DB = {
    DBMaker.fileDB(new File(mapDbFileName))
      .compressionEnable()
      .fileMmapEnable()
      .checksumEnable()
      .make()
  }

  /**
    * Init MapDB Store, either as Storage used in [[MapDbStoreBackend]], or during migration to DB.
    *
    * @param db opened MapDB instance
    * @return opened MapDB Store instance
    */
  protected def initMapDBStore(db: DB): HTreeMap[String, Array[Byte]] = {
    db.hashMapCreate("values")
      .keySerializer(Serializer.STRING)
      .valueSerializer(new Serializer.CompressionWrapper(Serializer.BYTE_ARRAY))
      .makeOrGet()
  }

}

object AbstractStoreBackend {

  /**
    * Types of implementations of Storage Backends
    */
  sealed trait StoreType

  case object DatabaseStoreType extends StoreType

  case object MapDbStoreType extends StoreType

}

package io.simplifier.plugin.keyValueStore.helper

import com.typesafe.config.Config
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend.StoreType
import io.simplifier.pluginbase.util.logging.Logging
import org.mapdb.{DB, DBMaker, HTreeMap, Serializer}

import java.io.File
import scala.util.{Failure, Success, Try}

/**
  * Abstract store backend defining interface for different store backend implementations.
  * Also contains logic to initialize mapdb file storage
  */
abstract class AbstractStoreBackend(config: Config, val storeType: StoreType) extends Logging{

  /**
    * filename of storage file from config used by MapDb Backend or migration to database
    */
  lazy val mapDbFileName: String = Try(config.getString("plugin.filename")) match {
    case Success(fn) => fn
    case Failure(e) if config.hasPath("plugin.filename") => throw e
    case _ =>
      logger.warn("Could not access the mapDB filename from the configuration " +
        "for path: [plugin.filename], using the value: [/tmp/kvstore] as a fallback.")
      "/tmp/kvstore"
  }

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
    * Init MapDB, either as Storage used in [[DatabaseStoreBackend]], or during migration to DB.
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
    * Init MapDB Store, either as Storage used in [[DatabaseStoreBackend]], or during migration to DB.
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

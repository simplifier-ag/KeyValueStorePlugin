package io.simplifier.plugin.keyValueStore.helper

import com.typesafe.config.Config
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend.DatabaseStoreType
import io.simplifier.plugin.keyValueStore.model.{KeyValueStoreValue, KeyValueStoreValueDao, PluginSchema}
import io.simplifier.plugin.keyValueStore.pluginBaseRelated.db.{DatabaseMigration, SquerylInit}
import io.simplifier.pluginbase.model.UserTransaction
import org.mapdb.DB

/**
  * Implementation of storage backend with database access.
  *
  * @param config          configuration containing map db storage path (for migration) and database configuration
  * @param keyValueDao     dao for key value pairs
  * @param userTransaction database transaction factory
  */
class DatabaseStoreBackend(config: Config,
                           keyValueDao: KeyValueStoreValueDao,
                           userTransaction: UserTransaction
                          ) extends AbstractStoreBackend(config: Config, DatabaseStoreType) {

  override def get(key: String): Option[Array[Byte]] = {
    keyValueDao.findByHashedKey(key).map(_.data)
  }

  override def put(key: String, value: Array[Byte]): Unit = {

    val entity: KeyValueStoreValue = KeyValueStoreValue(key, value)

    userTransaction.inSingleTransaction {
      keyValueDao.findByHashedKey(key) match {
        case Some(existingEntity) => keyValueDao.update(existingEntity.copy(data = value))
        case None => keyValueDao.insert(entity)
      }
    }.get
  }

  override def exists(key: String): Boolean = {
    keyValueDao.checkHashedKeyExists(key)
  }

  override def delete(key: String): Unit = {
    keyValueDao.deleteByHashedKey(key)
  }

  override def list: Seq[String] = {
    keyValueDao.getAllKeys()
  }

  override def init(): Unit = {
    SquerylInit.initWith(SquerylInit.parseConfig(config))
    DatabaseMigration(config, PluginSchema).runMigration()
  }

  override def migrateToDatabase(): Unit = {
    val mapDb: DB = initMapDB()
    try {
      val mapDbStore = initMapDBStore(mapDb)
      userTransaction.inSingleTransaction {
        mapDbStore.forEach {
          case (key, value) =>
            val model = KeyValueStoreValue(key, value)
            keyValueDao.findByHashedKey(key) match {
              case Some(_) => keyValueDao.update(model)
              case None => keyValueDao.insert(model)
            }
        }
      }
    } finally {
      mapDb.close()
    }
  }
}

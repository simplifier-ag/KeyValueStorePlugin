package db.migration.common

import io.simplifier.plugin.keyValueStore.KeyValueStorePlugin
import io.simplifier.plugin.keyValueStore.pluginBaseRelated.data.Checksum
import io.simplifier.plugin.keyValueStore.pluginBaseRelated.db.SquerylInit
import io.simplifier.pluginbase.util.logging.Logging
import org.flywaydb.core.api.migration.{BaseJavaMigration, Context}
import org.mapdb.{DBMaker, HTreeMap, Serializer}
import org.squeryl.PrimitiveTypeMode.inTransaction

import java.io.{ByteArrayInputStream, File}
import java.nio.charset.StandardCharsets
import scala.util.{Failure, Success, Try}

abstract class V1_1__Migrate_To_Database extends BaseJavaMigration with Logging {

  val insertStatement: String
  val tablePrefix: String = SquerylInit.tablePrefix.find(_.nonEmpty).getOrElse("")

  override def migrate(context: Context): Unit = inTransaction {
    var counter: Int = 0
    var size: Long = 0
    val maxSize: Long = 104857600
    val connection = context.getConnection
    val config = KeyValueStorePlugin.BASIC_STATE.config
    val mapDbFileName: String = Try(config.getString("plugin.filename")) match {
      case Success(fn) => fn
      case Failure(e) if config.hasPath("plugin.filename") => throw e
      case _ =>
        logger.warn("Could not access the mapDB filename from the configuration " +
          "for path: [plugin.filename], using the value: [/tmp/kvstore] as a fallback.")
        "/tmp/kvstore"
    }
    logger.info("setup")
    val mapDb = DBMaker.fileDB(new File(mapDbFileName))
      .compressionEnable()
      .fileMmapEnable()
      .checksumEnable()
      .make()
    logger.info("made file")
    try {
      val mapDbStore: HTreeMap[String, Array[Byte]] = mapDb.hashMapCreate("values")
        .keySerializer(Serializer.STRING)
        .valueSerializer(new Serializer.CompressionWrapper(Serializer.BYTE_ARRAY))
        .makeOrGet()
      logger.info("got mapdbstore")
      mapDbStore.keySet().forEach { key =>
        Try(mapDbStore.get(key)) match {
          case Failure(exception) => logger.warn(s"value for key $key could not be read. ${exception.getMessage}")
          case Success(value) => size += value.length
            counter += 1
            if (counter % 100 == 0 || size >= maxSize) {
              logger.debug(s"$counter")
              logger.debug(s"$size")
              size = value.length
              connection.commit()
            }
            val valueStream = new ByteArrayInputStream(value)
            val prepInsertStatement = connection.prepareStatement(insertStatement)
            prepInsertStatement.setString(1, encodeKey(key))
            prepInsertStatement.setString(2, key)
            prepInsertStatement.setBlob(3, valueStream)
            try {
              prepInsertStatement.execute()
            } finally {
              prepInsertStatement.close()
            }
        }
      }
    } finally {
      mapDb.close()
    }

  }

  private def encodeKey(key: String): String = {
    val item = key.getBytes(StandardCharsets.UTF_8)
    Checksum.checksumSHA256(item)
  }

}

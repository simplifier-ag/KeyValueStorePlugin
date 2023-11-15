package io.simplifier.plugin.keyValueStore.helper

import com.typesafe.config.Config
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend.MapDbStoreType

import scala.collection.JavaConverters._

/**
  * Key Value Store backend using MapDB (storing data in file in filesystem)
  */
class MapDbStoreBackend(config: Config) extends AbstractStoreBackend(config: Config, MapDbStoreType) {

  import org.mapdb._

  protected var store: Option[HTreeMap[String, Array[Byte]]] = None

  val db: DB = initMapDB()

  override def init(): Unit = {
    // init db
    store = Some(initMapDBStore(db))
  }

  override def shutdown(): Unit = db.close()

  override def get(key: String): Option[Array[Byte]] = store match {
    case Some(s) => if (s.containsKey(key))
      Some(s.get(key))
    else
      None
    case _ => None
  }

  override def put(key: String, value: Array[Byte]): Unit = store match {
    case Some(s) =>
      s.put(key, value)
      db.commit()
    case _ =>
  }

  override def exists(key: String): Boolean = store match {
    case Some(s) => s.containsKey(key)
    case _ => false
  }

  override def delete(key: String): Unit = store match {
    case Some(s) => s.remove(key)
    case _ =>
  }

  override def list: Seq[String] = store map {
    _.keySet.asScala.toSeq
  } getOrElse Seq()

  override def migrateToDatabase(): Unit = new NotImplementedError("Migration cannot be executed with MapDb backend")
}

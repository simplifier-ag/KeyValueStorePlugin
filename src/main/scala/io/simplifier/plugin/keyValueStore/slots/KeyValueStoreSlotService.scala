package io.simplifier.plugin.keyValueStore.slots

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import io.simplifier.plugin.keyValueStore.Constants.{ACTION_EXISTS, ACTION_GRACEFULLY_SHUTDOWN, ACTION_MIGRATE, ACTION_MIGRATED, ACTION_PUT, ACTION_TEST, KEY_VALUE}
import io.simplifier.plugin.keyValueStore.RestMessages._
import io.simplifier.plugin.keyValueStore.controller.KeyValueStoreController
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend.{DatabaseStoreType, MapDbStoreType}
import io.simplifier.plugin.keyValueStore.permission.PermissionHandler
import io.simplifier.pluginapi.UserSession
import io.simplifier.pluginapi.rest.PluginHeaders
import io.simplifier.pluginbase.PluginSettings
import io.simplifier.pluginbase.interfaces.AppServerDispatcher
import io.simplifier.pluginbase.slotservice.Constants.{ACTION_DELETE, ACTION_GET, ACTION_LIST}
import io.simplifier.pluginbase.util.api.SuccessMessage

class KeyValueStoreSlotService(keyValueStoreController: KeyValueStoreController) extends GenericKeyValueSlotService {

  val dbSlotNames: Seq[String] = Seq("migrate")

  override def slotNames: Seq[String] = Seq(
    "get", "gethttp",
    "put", "puthttp",
    "exists", "existshttp",
    "delete", "deletehttp",
    "gracefullyShutdown", "gracefullyShutdownhttp",
    "test",
    "list", "listhttp") ++
    (keyValueStoreController.storeBackend.storeType match {
      case DatabaseStoreType => dbSlotNames
      case _ => Nil
    })

  def standaloneOnlySlotNames: Option[Set[String]] = {
    keyValueStoreController.storeBackend.storeType match {
      case DatabaseStoreType =>
        Some(dbSlotNames.toSet)
      case MapDbStoreType =>
        Some(slotNames.toSet)
    }
  }

  override def serviceRoute(implicit requestSource: PluginHeaders.RequestSource, userSession: UserSession): Route =
    keyValueStoreController.storeBackend.storeType match {
      case DatabaseStoreType => clusterableRoute
      case _ => standaloneRoute
    }

  private def standaloneRoute(implicit requestSource: PluginHeaders.RequestSource, userSession: UserSession): Route =
    path("gethttp" | "get") {
      requestHandler(keyValueStoreController.get, getKeyValueFailure, ACTION_GET, KEY_VALUE)
    } ~
      path("puthttp" | "put") {
        asyncRequestHandler(keyValueStoreController.put, putKeyValueFailure, ACTION_PUT, KEY_VALUE)
      } ~
      path("existshttp" | "exists") {
        requestHandler(keyValueStoreController.exists, existsKeyValueFailure, ACTION_EXISTS, KEY_VALUE)
      } ~
      path("deletehttp" | "delete") {
        requestHandler(keyValueStoreController.delete, deleteKeyValueFailure, ACTION_DELETE, KEY_VALUE)
      } ~
      path("gracefullyShutdownhttp" | "gracefullyShutdown") {
        complete(resultHandler(keyValueStoreController.gracefullyShutdown, gracefullyShutdownKeyValueFailure, ACTION_GRACEFULLY_SHUTDOWN, KEY_VALUE))
      } ~
      path("test") {
        complete(resultHandler(keyValueStoreController.test, testKeyValueFailure, ACTION_TEST, KEY_VALUE))
      } ~
      path("listhttp" | "list") {
        complete(resultHandler(keyValueStoreController.list, listKeyValueFailure, ACTION_LIST, KEY_VALUE))
      }

  private def clusterableRoute(implicit requestSource: PluginHeaders.RequestSource, userSession: UserSession): Route =
    standaloneRoute ~
      path("migrate") {
        complete(resultHandler(keyValueStoreController.migrateToDatabase().map(_ => SuccessMessage(ACTION_MIGRATED)), migrateKeyValueFailure, ACTION_MIGRATE, KEY_VALUE))
      }

}

object KeyValueStoreSlotService {
  def apply(config: Config, dispatcher: AppServerDispatcher, pluginSettings: PluginSettings, storeBackend: AbstractStoreBackend)
           (implicit materializer: Materializer): KeyValueStoreSlotService =
    new KeyValueStoreSlotService(new KeyValueStoreController(storeBackend, dispatcher, pluginSettings,
      PermissionHandler(dispatcher, pluginSettings)))
}

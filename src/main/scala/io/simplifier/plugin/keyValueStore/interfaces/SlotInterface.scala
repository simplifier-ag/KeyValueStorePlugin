package io.simplifier.plugin.keyValueStore.interfaces

import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend
import io.simplifier.plugin.keyValueStore.permission.KeyValueStorePluginPermission.characteristicAdministrate
import io.simplifier.plugin.keyValueStore.permission.PermissionHandler
import io.simplifier.plugin.keyValueStore.slots.KeyValueStoreSlotService
import io.simplifier.pluginapi.UserSession
import io.simplifier.pluginapi.rest.PluginHeaders
import io.simplifier.pluginapi.rest.PluginHeaders.RequestSource
import io.simplifier.pluginbase.interfaces.{AppServerDispatcher, SlotInterfaceService}
import io.simplifier.pluginbase.permission.PluginPermissionObject
import io.simplifier.pluginbase.{PluginDescription, PluginSettings}

class SlotInterface(dispatcher: AppServerDispatcher,
                    pluginDescription: PluginDescription,
                    pluginPermission: PluginPermissionObject,
                    keyValueStoreSlotService: KeyValueStoreSlotService,
                    permissionHandler: PermissionHandler)
  extends SlotInterfaceService(dispatcher, pluginDescription, pluginPermission) {

  /** Base-URL relative to http service root */
  override val baseUrl: String = "slots"

  override def pluginSlotNames: Seq[String] =
    keyValueStoreSlotService.slotNames

  override protected def checkAdministratePermission()(implicit userSession: UserSession, requestSource: RequestSource): Unit = {
    permissionHandler.checkAdditionalPermission(characteristicAdministrate)
  }

  override def standaloneOnlySlots: Option[Set[String]] = keyValueStoreSlotService.standaloneOnlySlotNames

  /**
    * Plugin-specific inner route handling slot requests
    *
    * @param requestSource plugin request source
    * @param userSession   authenticated user session
    * @return service route
    */
  override def serviceRoute(implicit requestSource: PluginHeaders.RequestSource, userSession: UserSession): Route = {
    keyValueStoreSlotService.serviceRoute
  }
}

object SlotInterface {

  def apply(dispatcher: AppServerDispatcher, pluginSettings: PluginSettings, config: Config,
            storeBackend: AbstractStoreBackend, pluginDescription: PluginDescription,
            pluginPermission: PluginPermissionObject)
           (implicit materializer: Materializer): SlotInterface = {
    new SlotInterface(dispatcher, pluginDescription, pluginPermission,
      KeyValueStoreSlotService(config, dispatcher, pluginSettings, storeBackend), new PermissionHandler(dispatcher, pluginSettings))
  }

}
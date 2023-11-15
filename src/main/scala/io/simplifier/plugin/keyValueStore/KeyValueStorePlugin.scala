package io.simplifier.plugin.keyValueStore

import io.simplifier.plugin.keyValueStore.helper.{AbstractStoreBackend, DatabaseStoreBackend}
import io.simplifier.plugin.keyValueStore.interfaces.SlotInterface
import io.simplifier.plugin.keyValueStore.model.KeyValueStoreValueDao
import io.simplifier.plugin.keyValueStore.permission.KeyValueStorePluginPermission
import io.simplifier.pluginbase.interfaces.{DocumentationInterfaceService, PluginBaseHttpService}
import io.simplifier.pluginbase.model.UserTransaction
import io.simplifier.pluginbase.permission.PluginPermissionObject
import io.simplifier.pluginbase.{SimplifierPlugin, SimplifierPluginLogic}

import scala.concurrent.Future

object KeyValueStorePlugin extends SimplifierPluginLogic(
  Defaults.PLUGIN_DESCRIPTION_DEFAULT, "keyValueStorePlugin") with SimplifierPlugin {

  val pluginSecret: String = byDeployment.PluginRegistrationSecret()

  import ACTOR_SYSTEM.dispatcher

  val permission = KeyValueStorePluginPermission

  /**
   * The plugin permissions
   *
   * @return a sequence of the plugin permissions
   */
  override def pluginPermissions: Seq[PluginPermissionObject] = Seq(permission)

  var storeBackend: AbstractStoreBackend = _

  override def stopPluginServices(): Future[Unit] = {
    storeBackend.shutdown()
    super.stopPluginServices()
  }

  override def startPluginServices(basicState: SimplifierPlugin.BasicState): Future[PluginBaseHttpService] = Future {

    storeBackend = new DatabaseStoreBackend(basicState.config, new KeyValueStoreValueDao, new UserTransaction)

    storeBackend.init()
    val slotInterface = Some(SlotInterface(basicState.dispatcher, basicState.settings, basicState.config,
      storeBackend, basicState.pluginDescription, permission))
    val proxyInterface = None
    val configInterface = None
    val documentationInterface = Some(new DocumentationInterfaceService {
      override val apiClasses: Set[Class[_]] = Set()
      override val title: String = "KeyValueStore Plugin Client API"
      override val description: String = "Plugin to persist data."
      override val externalDocsDescription: String = "Documentation for KeyValueStore Plugin"
    })
    new PluginBaseHttpService(basicState.pluginDescription, basicState.settings, basicState.appServerInformation, proxyInterface, slotInterface, configInterface, documentationInterface)
  }

}
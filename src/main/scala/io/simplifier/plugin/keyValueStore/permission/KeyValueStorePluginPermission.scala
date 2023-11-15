package io.simplifier.plugin.keyValueStore.permission

import io.simplifier.pluginbase.permission.PluginPermissionObject
import io.simplifier.pluginbase.permission.PluginPermissionObjectCharacteristics.CheckboxCharacteristic

object KeyValueStorePluginPermission extends PluginPermissionObject {

  val characteristicAdministrate = "administrate"

  val characteristicView = "view"

  val characteristicEdit = "edit"

  /**
    * Name of the permission object.
    */
  override val name: String = "Key Value Store Plugin"
  /**
    * Technical Name of the permission object.
    */
  override val technicalName: String = PluginPermissionObject.getTechnicalName("KeyValueStore Plugin")
  /**
    * Description of the permission object.
    */
  override val description: String = "Plugin: Handle permissions for the Key Value Store"
  /**
    * Possible characteristics for the admin ui.
    */
  override val characteristics: Seq[CheckboxCharacteristic] = Seq(
    CheckboxCharacteristic(characteristicAdministrate, "Administrate", "Administrate the plugin"),
    CheckboxCharacteristic(characteristicView, "View", "View exiting key value data"),
    CheckboxCharacteristic(characteristicEdit, "Edit", "Create, edit and delete key value data")
  )
}

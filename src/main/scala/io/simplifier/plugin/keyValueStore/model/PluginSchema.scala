package io.simplifier.plugin.keyValueStore.model

import io.simplifier.plugin.keyValueStore.pluginBaseRelated.db.SquerylInit
import org.squeryl.{Schema, Table}

/**
  * DB Schema for Plugin Data Model.
  */
object PluginSchema extends PluginSchema

/**
  * DB Schema for Plugin Data Model.
  */
class PluginSchema extends Schema {

  val prefix: String = SquerylInit.tablePrefix.getOrElse("")

  /*
   * Tables
   */

  val valueT: Table[KeyValueStoreValue] = table[KeyValueStoreValue](prefix + "Key_Value_Store_Value")

}

package io.simplifier.plugin.keyValueStore.pluginBaseRelated.db

/**
 * Trait to indicate entities which contain a name value (or definition).
 */
trait NamedEntity {

  def name: String

}
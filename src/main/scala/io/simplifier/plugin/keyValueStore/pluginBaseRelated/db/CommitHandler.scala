package io.simplifier.plugin.keyValueStore.pluginBaseRelated.db

/**
  * Handler trait encapsulating a commit handling method.
  */
trait CommitHandler {

  /**
    * Execute handler after a successful commit
    */
  def handleConnectionCommit(): Unit

}






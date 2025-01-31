package io.simplifier.plugin.keyValueStore

object Constants {

  val KEY_VALUE = "keyValue"

  val ACTION_EXISTED = "existed"
  val ACTION_EXISTS = "exists"
  val ACTION_GRACEFULLY_SHUTDOWN = "gracefullyShutdown"
  val ACTION_PUT = "put"
  val ACTION_TEST = "test"
  val ACTION_MIGRATE = "migrate"
  val ACTION_MIGRATED = "migrated"

  val KEY_VALUE_NOT_FOUND: String = "A-001"
  val MISSING_PARAMETER_FAILURE: String = "A-002"
  val FILE_NOT_FOUND: String = "A-003"
}

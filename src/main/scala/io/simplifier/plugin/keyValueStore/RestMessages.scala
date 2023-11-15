package io.simplifier.plugin.keyValueStore

import io.simplifier.plugin.keyValueStore.Constants._
import io.simplifier.pluginbase.slotservice.Constants.{ACTION_DELETED, ACTION_LISTED, ACTION_RETURNED}
import io.simplifier.pluginbase.slotservice.GenericRestMessages

object RestMessages extends GenericRestMessages {

  val (deleteKeyValueSuccess, deleteKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_DELETED)
  val (getKeyValueSuccess, getKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_RETURNED)
  val (existsKeyValueSuccess, existsKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_EXISTED)
  val (gracefullyShutdownKeyValueSuccess, gracefullyShutdownKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_GRACEFULLY_SHUTDOWN)
  val (listKeyValueSuccess, listKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_LISTED)
  val (putKeyValueSuccess, putKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_PUT)
  val (testKeyValueSuccess, testKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_TEST)
  val (migrateKeyValueSuccess, migrateKeyValueFailure) = mkRestMessagePair(KEY_VALUE, ACTION_MIGRATED)

}

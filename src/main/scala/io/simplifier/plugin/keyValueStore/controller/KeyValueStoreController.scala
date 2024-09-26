package io.simplifier.plugin.keyValueStore.controller

import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import io.simplifier.plugin.keyValueStore.{Constants, KeyValueStorePlugin}
import io.simplifier.plugin.keyValueStore.helper.AbstractStoreBackend
import io.simplifier.plugin.keyValueStore.permission.KeyValueStorePluginPermission.{characteristicAdministrate, characteristicEdit, characteristicView}
import io.simplifier.plugin.keyValueStore.permission.PermissionHandler
import io.simplifier.pluginapi.UserSession
import io.simplifier.pluginapi.helper.Base64Encoding
import io.simplifier.pluginapi.rest.PluginHeaders.RequestSource
import io.simplifier.pluginbase.PluginSettings
import io.simplifier.pluginbase.interfaces.AppServerDispatcher
import io.simplifier.pluginbase.slotservice.GenericFailureHandling.OperationFailureMessage
import io.simplifier.pluginbase.slotservice.GenericRestMessages.RestMessage
import io.simplifier.pluginbase.util.api.ApiMessage
import io.simplifier.pluginbase.util.json.JSONCompatibility.parseJsonOrEmptyString
import io.simplifier.pluginbase.util.json.SimplifierFormats
import org.json4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Success, Try}

class KeyValueStoreController(val storeBackend: AbstractStoreBackend, dispatcher: AppServerDispatcher,
                              pluginSettings: PluginSettings, permissionHandler: PermissionHandler)
                             (implicit materializer: Materializer) extends Base64Encoding with SimplifierFormats {

  import KeyValueStoreController._

  def delete(request: DeleteByKey)(implicit userSession: UserSession, requestSource: RequestSource): Try[Result] = Try {
    permissionHandler.checkAdditionalPermission(characteristicEdit)
    storeBackend.delete(request.key)
    new Result("ok")
  }

  def exists(request: ExistsByKey)(implicit userSession: UserSession, requestSource: RequestSource): Try[Result] = Try {
    permissionHandler.checkAdditionalPermission(characteristicView)
    new Result(KeyFound(storeBackend.exists(request.key)))
  }

  def get(request: GetByKey)(implicit userSession: UserSession, requestSource: RequestSource): Try[Result] = Try {
    permissionHandler.checkAdditionalPermission(characteristicView)
    storeBackend.get(request.key) match {
      case Some(res) =>
        new Result(encodeB64(res))
      case _ =>
        throw KeyNotFound
    }
  }

  def gracefullyShutdown()(implicit userSession: UserSession, requestSource: RequestSource): Try[ApiMessage] = {
    permissionHandler.checkAdditionalPermission(characteristicAdministrate)
    storeBackend.shutdown()
    KeyValueStorePlugin.terminateBySystemShutdown()
    Success(new Result("ByeBye"))
  }

  def list()(implicit userSession: UserSession, requestSource: RequestSource): Try[ApiMessage] = {
    permissionHandler.checkAdditionalPermission(characteristicView)
    val list = storeBackend.list.mkString(", ")
    Success(new Result(list))
  }

  private val Base64Regex = "^([a-zA-Z0-9+/]{4}|[a-zA-Z0-9+/]{3}=|[a-zA-Z0-9+/]{2}==)*$".r

  def put(request: PutByKey)(implicit userSession: UserSession, requestSource: RequestSource): Future[Result] = {
    permissionHandler.checkAdditionalPermission(characteristicEdit)
    val putData: Array[Byte] => Result = data => {
      storeBackend.put(request.key, data)
      new Result("ok")
    }
    (request.content, request.uploadSession, request.copyFrom) match {
      case (Some(Base64Regex(value)), _, _) =>
        Future.successful(putData(decodeB64(value)))
      case (Some(_), _, _) =>
        Future.failed(InvalidInputFormat)
      case (_, Some(uploadSession), _) =>
        dispatcher.downloadAssetAsByteArray(uploadSession) map putData
      case (_, _, Some(copyKey)) =>
        Future.successful(storeBackend.get(copyKey)) map {
          case Some(data) => putData(data)
          case None => throw FileNotFound
        }
      case _ =>
        Future.failed(MissingParameterFailure)
    }
  }

  def test()(implicit userSession: UserSession, requestSource: RequestSource): Try[ApiMessage] = Try {
    permissionHandler.checkAdditionalPermission(characteristicView)
    val request: HttpRequest = Get("/api/information/version")
    val future = dispatcher.sendViaSimplifierFlow(request) map { response =>
      new Result(parseJsonOrEmptyString(AppServerDispatcher.renderEntity(response)))
    }
    Await.result(future, pluginSettings.timeoutDuration)
  }

  def migrateToDatabase()(implicit userSession: UserSession, requestSource: RequestSource): Try[Unit] = Try {
    permissionHandler.checkAdditionalPermission(characteristicAdministrate)
    storeBackend.migrateToDatabase()
  }

}

object KeyValueStoreController {

  case class Response(message: RestMessage, success: Boolean = true) extends ApiMessage

  case class ExistsByKey(key:String) extends ApiMessage
  case class KeyFound(found:Boolean) extends ApiMessage
  case class GetByKey(key:String) extends ApiMessage
  case class DeleteByKey(key:String) extends ApiMessage
  case class PutByKey(key:String, content: Option[String], uploadSession: Option[String], copyFrom: Option[String]) extends ApiMessage

  case class Result(result:JValue, success:Boolean) extends ApiMessage {
    def this(result: JValue) = this(result, true)
    def this(result: String) = this(JString(result))
    def this(result: Any) = this(Extraction.decompose(result)(DefaultFormats.lossless))
  }

  def KeyNotFound: OperationFailureMessage =
    OperationFailureMessage(s"key not found", Constants.KEY_VALUE_NOT_FOUND)

  def MissingParameterFailure: OperationFailureMessage =
    OperationFailureMessage(s"Missing parameter 'content', 'uploadSession' or 'copyFrom'", Constants.MISSING_PARAMETER_FAILURE)

  def FileNotFound: OperationFailureMessage =
    OperationFailureMessage(s"File to copy from not found", Constants.KEY_VALUE_NOT_FOUND)

  def InvalidInputFormat: OperationFailureMessage =
    OperationFailureMessage(s"Parameter 'content' must be base-64 encoded", Constants.INVALID_INPUT_FORMAT)

}

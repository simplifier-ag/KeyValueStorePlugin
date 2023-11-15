package io.simplifier.plugin.keyValueStore.slots

import akka.http.scaladsl.server.Directives.{as, complete, entity, _}
import akka.http.scaladsl.server.Route
import io.simplifier.plugin.keyValueStore.Constants
import io.simplifier.pluginbase.slotservice.Constants.{ERROR_CODE_MAPPING_EXCEPTION, ERROR_CODE_MISSING_PERMISSION}
import io.simplifier.pluginbase.slotservice.GenericFailureHandling.{OperationFailure, OperationFailureResponse}
import io.simplifier.pluginbase.slotservice.GenericRestMessages.RestMessage
import io.simplifier.pluginbase.slotservice.GenericSlotService
import io.simplifier.pluginbase.util.api.ApiMessage
import io.simplifier.pluginbase.util.http.JsonMarshalling._
import org.json4s._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

abstract class GenericKeyValueSlotService extends GenericSlotService {

  import GenericKeyValueSlotService._

  override def requestHandler[T <: ApiMessage](function: T => Try[ApiMessage],
                                               error: String => RestMessage,
                                               action: String, aspect: String)(implicit manifest: Manifest[T]): Route ={
    entity(as[T]) { request =>
      val result = resultHandler(function(request), error, action, aspect) match {
        case ofr: OperationFailureResponse => KeyValuePluginErrorResponse(ofr)
        case res: ApiMessage => res
      }
      complete(result)
    } ~
      entity(as[JValue]) { request =>
        Try {
          request.extract[T]
        } match {
          case Failure(ex) => complete(OperationFailure(error(ex.getMessage), ERROR_CODE_MAPPING_EXCEPTION).toResponse)
          case Success(extractedRequest) => complete(resultHandler(function(extractedRequest), error, action, aspect))
        }
      }
  }

  override def asyncRequestHandler[T <: ApiMessage](function: T => Future[ApiMessage],
                                           error: String => RestMessage,
                                           action: String, aspect: String)(implicit manifest: Manifest[T]): Route ={
    entity(as[T]) { request =>
      onComplete(function(request)) { data =>
        val result = resultHandler(data, error, action, aspect) match {
          case ofr: OperationFailureResponse => KeyValuePluginErrorResponse(ofr)
          case res: ApiMessage => res
        }
        complete(result)
      }
    } ~
      entity(as[JValue]) { request =>
        Try {
          request.extract[T]
        } match {
          case Failure(ex) => complete(OperationFailure(error(ex.getMessage), ERROR_CODE_MAPPING_EXCEPTION).toResponse)
          case Success(extractedRequest) =>
            onComplete(function(extractedRequest)) { data =>
              complete(resultHandler(data, error, action, aspect))
            }
        }
      }
  }


}

object GenericKeyValueSlotService {

  case class KeyValuePluginErrorResponse(code: Int, message: String, errorCode: String,
                                    errorMessage: RestMessage, success: Boolean = false) extends ApiMessage

  object KeyValuePluginErrorResponse {

    def getCodeAndMessage(errorCode: String): (Int, String) = {
      errorCode match {
        case Constants.KEY_VALUE_NOT_FOUND => (1, "key not found")
        case ERROR_CODE_MISSING_PERMISSION => (403, "forbidden")
        case _ => (500, "Unexpected exception")
      }
    }

    def apply(ofr: OperationFailureResponse): KeyValuePluginErrorResponse = {
      val (code, message) = getCodeAndMessage(ofr.errorCode)
      KeyValuePluginErrorResponse(code, message, ofr.errorCode, ofr.message)
    }
  }

}

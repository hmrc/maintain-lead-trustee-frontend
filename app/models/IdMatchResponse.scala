/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

sealed trait IdMatchResponse

case class SuccessfulOrUnsuccessfulMatchResponse(id: String,
                                                 idMatch: Boolean) extends IdMatchResponse

object SuccessfulOrUnsuccessfulMatchResponse {
  implicit val format: Format[SuccessfulOrUnsuccessfulMatchResponse] = Json.format[SuccessfulOrUnsuccessfulMatchResponse]
}

case class IdMatchErrorResponse(errors: Seq[String]) extends IdMatchResponse {
  override def toString: String = errors.mkString(", ")
}

object IdMatchErrorResponse {
  implicit val format: Format[IdMatchErrorResponse] = Json.format[IdMatchErrorResponse]
}

case object InvalidIdMatchResponse extends IdMatchResponse
case object AttemptLimitExceededResponse extends IdMatchResponse
case object NinoNotFoundResponse extends IdMatchResponse
case object InternalServerErrorResponse extends IdMatchResponse
case object ServiceUnavailableResponse extends IdMatchResponse

object IdMatchResponse extends Logging {

  implicit lazy val httpReads: HttpReads[IdMatchResponse] = (_: String, _: String, response: HttpResponse) => {
    logger.info(s"[IdMatchResponse] response status received from trusts-individual-check api: ${response.status}")

    lazy val errorLog: String = {
      val errorMessage: String = if (response.body.isEmpty) {
        "No error messages to parse"
      } else {
        response.json.validate[IdMatchErrorResponse] match {
          case JsSuccess(idMatchErrors, _) => s"Errors: ${idMatchErrors.toString}"
          case JsError(errors) => s"Unable to parse error messages: $errors"
        }
      }
      s"[IdMatchResponse] $errorMessage"
    }

    response.status match {
      case OK =>
        response.json.validate[SuccessfulOrUnsuccessfulMatchResponse] match {
          case JsSuccess(idMatchResponse, _) =>
            idMatchResponse
          case JsError(errors) =>
            logger.warn(s"Unable to parse response: $errors")
            InternalServerErrorResponse
        }
      case BAD_REQUEST =>
        logger.warn(errorLog)
        InvalidIdMatchResponse
      case FORBIDDEN =>
        logger.warn(errorLog)
        AttemptLimitExceededResponse
      case NOT_FOUND =>
        logger.warn(errorLog)
        NinoNotFoundResponse
      case SERVICE_UNAVAILABLE =>
        logger.error(errorLog)
        ServiceUnavailableResponse
      case _ =>
        logger.error(errorLog)
        InternalServerErrorResponse
    }
  }
}

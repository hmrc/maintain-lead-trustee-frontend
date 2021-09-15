/*
 * Copyright 2021 HM Revenue & Customs
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

package services

import config.FrontendAppConfig
import connectors.TrustsIndividualCheckConnector
import models._
import pages.leadtrustee.individual.{NamePage, NationalInsuranceNumberPage, TrusteesDateOfBirthPage}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustsIndividualCheckService @Inject()(connector: TrustsIndividualCheckConnector)
                                            (implicit config: FrontendAppConfig) extends Logging {

  def matchLeadTrustee(userAnswers: UserAnswers)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustsIndividualCheckServiceResponse] = {

    val body: Option[IdMatchRequest] = for {
      id <- generateId(userAnswers.identifier)
      nino <- userAnswers.get(NationalInsuranceNumberPage)
      name <- userAnswers.get(NamePage)
      dob <- userAnswers.get(TrusteesDateOfBirthPage)
    } yield {
      IdMatchRequest(
        id = id,
        nino = nino.toUpperCase,
        surname = name.lastName.capitalize,
        forename = name.firstName.capitalize,
        birthDate = dob.toString
      )
    }

    body match {
      case Some(idMatchRequest) if idMatchRequest.isBirthDateAcceptable =>
        connector.matchLeadTrustee(idMatchRequest) map {
          case SuccessfulOrUnsuccessfulMatchResponse(_, true) => SuccessfulMatchResponse
          case SuccessfulOrUnsuccessfulMatchResponse(_, false) | NinoNotFoundResponse => UnsuccessfulMatchResponse
          case AttemptLimitExceededResponse => LockedMatchResponse
          case ServiceUnavailableResponse => ServiceUnavailableErrorResponse
          case _ => MatchingErrorResponse
        }
      case Some(_) =>
        logger.error(s"[matchLeadTrustee] date of birth is before ${config.minLeadTrusteeDob}")
        Future.successful(IssueBuildingPayloadResponse)
      case _ =>
        logger.error(s"[matchLeadTrustee] Unable to build request body.")
        Future.successful(IssueBuildingPayloadResponse)
    }
  }

  def failedAttempts(identifier: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] = {

    generateId(identifier) match {
      case Some(id) =>
        connector.failedAttempts(id)
      case _ =>
        Future.failed(new Throwable("Failed to extract session ID from header carrier."))
    }
  }

  private def generateId(identifier: String)(implicit hc: HeaderCarrier): Option[String] = {
    hc.sessionId map { sessionId =>
      s"${sessionId.value}~$identifier"
    }
  }
}

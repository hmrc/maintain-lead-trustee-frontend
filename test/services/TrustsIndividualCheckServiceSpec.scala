/*
 * Copyright 2026 HM Revenue & Customs
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

import java.time.LocalDate
import base.SpecBase
import config.FrontendAppConfig
import connectors.TrustsIndividualCheckConnector
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.RecoverMethods.recoverToSucceededIf
import org.scalatest.concurrent.ScalaFutures
import pages.leadtrustee.individual._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.Future

class TrustsIndividualCheckServiceSpec extends SpecBase with ScalaFutures {

  private val sessionId = "sessionId"
  private val identifier = emptyUserAnswers.identifier
  private val id = s"$sessionId~$identifier"

  override implicit lazy val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))

  private val firstName = "joe"
  private val firstNameCapitalised = "Joe"
  private val lastName = "bloggs"
  private val lastNameCapitalised = "Bloggs"
  private val nino = "aa000000a"
  private val ninoUpperCase = "AA000000A"
  private val date = "1996-02-03"

  private val idMatchRequest = IdMatchRequest(
    id = id,
    nino = ninoUpperCase,
    surname = lastNameCapitalised,
    forename = firstNameCapitalised,
    birthDate = date
  )

  private implicit val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  "TrustsIndividualCheck" when {

    ".matchLeadTrustee" when {

      "sufficient data to assemble IdMatchRequest body" must {

        val userAnswers = emptyUserAnswers
          .set(NamePage, Name(firstName, None, lastName)).success.value
          .set(TrusteesDateOfBirthPage, LocalDate.parse(date)).success.value
          .set(NationalInsuranceNumberPage, nino).success.value

        "return SuccessfulMatchResponse" when {
          "successfully matched" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(SuccessfulOrUnsuccessfulMatchResponse(id, idMatch = true)))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe SuccessfulMatchResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }
        }

        "return UnsuccessfulMatchResponse" when {

          "unsuccessfully matched" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(SuccessfulOrUnsuccessfulMatchResponse(id, idMatch = false)))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe UnsuccessfulMatchResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }

          "NINO not found" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(NinoNotFoundResponse))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe UnsuccessfulMatchResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }
        }

        "return LockedMatchResponse" when {
          "attempt limit exceeded" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(AttemptLimitExceededResponse))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe LockedMatchResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }
        }

        "return ServiceUnavailableErrorResponse" when {
          "service unavailable" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(ServiceUnavailableResponse))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe ServiceUnavailableErrorResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }
        }

        "return MatchingErrorResponse" when {

          "invalid match ID" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(InvalidIdMatchResponse))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe MatchingErrorResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }

          "internal server error" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            when(mockConnector.matchLeadTrustee(any())(any(), any()))
              .thenReturn(Future.successful(InternalServerErrorResponse))

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe MatchingErrorResponse
              verify(mockConnector).matchLeadTrustee(eqTo(idMatchRequest))(any(), any())
            }
          }
        }
      }

      "insufficient data to assemble IdMatchRequest body" must {
        "return IssueBuildingPayloadResponse" when {

          "invalid date of birth" in {

            val date = "1500-01-01"

            val userAnswers = emptyUserAnswers
              .set(NamePage, Name(firstName, None, lastName)).success.value
              .set(TrusteesDateOfBirthPage, LocalDate.parse(date)).success.value
              .set(NationalInsuranceNumberPage, nino).success.value

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            val result = service.matchLeadTrustee(userAnswers)

            whenReady(result) { res =>
              res mustBe IssueBuildingPayloadResponse
              verify(mockConnector, never).matchLeadTrustee(any())(any(), any())
            }
          }

          "missing data" in {

            val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
            val service = new TrustsIndividualCheckService(mockConnector)

            val result = service.matchLeadTrustee(emptyUserAnswers)

            whenReady(result) { res =>
              res mustBe IssueBuildingPayloadResponse
              verify(mockConnector, never()).matchLeadTrustee(any())(any(), any())
            }
          }
        }
      }
    }

    ".failedAttempts" when {

      "header carrier has a session ID" must {
        "make call to connector" in {

          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))

          val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
          val service = new TrustsIndividualCheckService(mockConnector)

          val numberOfFailedAttempts = 1

          when(mockConnector.failedAttempts(any())(any(), any()))
            .thenReturn(Future.successful(numberOfFailedAttempts))

          val result = service.failedAttempts(identifier)

          whenReady(result) { res =>
            res mustBe numberOfFailedAttempts
            verify(mockConnector).failedAttempts(any())(any(), any())
          }
        }
      }

      "header carrier doesn't have a session ID" must {
        "throw error" in {

          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = None)

          val mockConnector = Mockito.mock(classOf[TrustsIndividualCheckConnector])
          val service = new TrustsIndividualCheckService(mockConnector)

          val result = service.failedAttempts(identifier)

          recoverToSucceededIf[Exception](result)
        }
      }
    }
  }
}

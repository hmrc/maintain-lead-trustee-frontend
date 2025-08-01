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

package controllers.leadtrustee.individual

import java.time.LocalDate
import base.SpecBase
import connectors.TrustConnector
import mapping.mappers.TrusteeMappers
import models.{LeadTrusteeIndividual, LeadTrusteeOrganisation, Name, NationalInsuranceNumber, UkAddress}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.mockito.Mockito
import pages.leadtrustee.IsReplacingLeadTrusteePage
import pages.leadtrustee.individual.IndexPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.checkYourAnswers.TrusteePrintHelpers
import viewmodels.AnswerSection
import views.html.leadtrustee.individual.CheckDetailsView

import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase {

  private lazy val onPageLoadRoute: Call = routes.CheckDetailsController.onPageLoad()
  private lazy val onPageLoadUpdatedRoute: Call = routes.CheckDetailsController.onPageLoadUpdated()
  private lazy val onSubmitRoute: Call = routes.CheckDetailsController.onSubmit()

  private val ukAddress = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")

  private val leadTrustee = LeadTrusteeIndividual(
    bpMatchStatus = None,
    name = Name("Joe", None, "Bloggs"),
    dateOfBirth = LocalDate.parse("2000-01-01"),
    phoneNumber = "tel",
    email = None,
    identification = NationalInsuranceNumber("nino"),
    address = ukAddress,
    countryOfResidence = None,
    nationality = None
  )

  private val leadTrusteeOrg = LeadTrusteeOrganisation(
    name = "Test Organisation",
    phoneNumber = "tel",
    email = None,
    utr = None,
    address = ukAddress,
    countryOfResidence = None
  )

  private val mockPrintHelper = Mockito.mock(classOf[TrusteePrintHelpers])
  private val mockMapper = Mockito.mock(classOf[TrusteeMappers])
  private val answerSection = AnswerSection(None, Nil)

  "CheckDetails Controller" when {

    ".onPageLoad" must {

      "return OK and the correct view for a GET" in {

        val mockTrustsConnector = Mockito.mock(classOf[TrustConnector])

        when(mockTrustsConnector.getLeadTrustee(any())(any(), any())).thenReturn(Future.successful(leadTrustee))
        when(mockPrintHelper.printLeadIndividualTrustee(any(), any())(any())).thenReturn(answerSection)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TrustConnector].toInstance(mockTrustsConnector),
            bind[TrusteePrintHelpers].toInstance(mockPrintHelper)
          ).build()

        val request = FakeRequest(GET, onPageLoadRoute.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckDetailsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection)(request, messages).toString
      }

      "return INTERNAL_SERVER_ERROR" when {
        "lead trustee is of type organisation" in {

          val mockTrustsConnector = Mockito.mock(classOf[TrustConnector])

          when(mockTrustsConnector.getLeadTrustee(any())(any(), any())).thenReturn(Future.successful(leadTrusteeOrg))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(GET, onPageLoadRoute.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }

        "error getting lead trustee" in {

          val mockTrustsConnector = Mockito.mock(classOf[TrustConnector])

          when(mockTrustsConnector.getLeadTrustee(any())(any(), any())).thenReturn(Future.failed(new Throwable("")))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(GET, onPageLoadRoute.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }

    ".onPageLoadUpdated" must {

      "return OK and the correct view for a GET" in {

        when(mockPrintHelper.printLeadIndividualTrustee(any(), any())(any())).thenReturn(answerSection)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrusteePrintHelpers].toInstance(mockPrintHelper))
          .build()

        val request = FakeRequest(GET, onPageLoadUpdatedRoute.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckDetailsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection)(request, messages).toString
      }
    }

    ".onSubmit" when {

      "mapper returns lead trustee" when {

        "amending" when {

          "amending existing lead trustee" must {

            "redirect to the next page" in {
              val mockTrustConnector = Mockito.mock(classOf[TrustConnector])

              val userAnswers = emptyUserAnswers

              when(mockMapper.mapToLeadTrusteeIndividual(any())).thenReturn(Some(leadTrustee))
              when(mockTrustConnector.amendLeadTrustee(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

              val application = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(
                  bind[TrustConnector].toInstance(mockTrustConnector),
                  bind[TrusteeMappers].toInstance(mockMapper)
                ).build()

              val request = FakeRequest(POST, onSubmitRoute.url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

              verify(mockTrustConnector).amendLeadTrustee(any(), eqTo(leadTrustee))(any(), any())
              verify(mockTrustConnector, never()).demoteLeadTrustee(any(), any())(any(), any())
            }
          }

          "replacing existing lead trustee" must {

            "redirect to the next page" in {
              val mockTrustConnector = Mockito.mock(classOf[TrustConnector])

              val userAnswers = emptyUserAnswers.set(IsReplacingLeadTrusteePage, true).success.value

              when(mockMapper.mapToLeadTrusteeIndividual(any())).thenReturn(Some(leadTrustee))
              when(mockTrustConnector.demoteLeadTrustee(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

              val application = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(
                  bind[TrustConnector].toInstance(mockTrustConnector),
                  bind[TrusteeMappers].toInstance(mockMapper)
                ).build()

              val request = FakeRequest(POST, onSubmitRoute.url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

              verify(mockTrustConnector).demoteLeadTrustee(any(), eqTo(leadTrustee))(any(), any())
            }
          }
        }

        "promoting" must {

          "redirect to the next page" in {

            val index = 0

            val mockTrustConnector = Mockito.mock(classOf[TrustConnector])

            val userAnswers = emptyUserAnswers.set(IndexPage, index).success.value

            when(mockMapper.mapToLeadTrusteeIndividual(any())).thenReturn(Some(leadTrustee))
            when(mockTrustConnector.promoteTrustee(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[TrustConnector].toInstance(mockTrustConnector),
                bind[TrusteeMappers].toInstance(mockMapper)
              ).build()

            val request = FakeRequest(POST, onSubmitRoute.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

            verify(mockTrustConnector).promoteTrustee(any(), eqTo(index), eqTo(leadTrustee))(any(), any())
          }
        }
      }

      "mapper returns None" must {

        "return InternalServerError" in {

          val userAnswers = emptyUserAnswers

          when(mockMapper.mapToLeadTrusteeIndividual(any())).thenReturn(None)

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrusteeMappers].toInstance(mockMapper))
            .build()

          val request = FakeRequest(POST, onSubmitRoute.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }

      "connector call" must {

        "return InternalServerError" in {

          val mockTrustConnector = Mockito.mock(classOf[TrustConnector])

          val userAnswers = emptyUserAnswers

          when(mockMapper.mapToLeadTrusteeIndividual(any())).thenReturn(Some(leadTrustee))
          when(mockTrustConnector.amendLeadTrustee(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[TrustConnector].toInstance(mockTrustConnector),
              bind[TrusteeMappers].toInstance(mockMapper)
            ).build()

          val request = FakeRequest(POST, onSubmitRoute.url)
          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}

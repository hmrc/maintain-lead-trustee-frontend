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

package controllers.trustee.individual.amend

import base.SpecBase
import connectors.TrustConnector
import mapping.mappers.TrusteeMappers
import models.{Name, TrusteeIndividual, TrusteeOrganisation}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.trustee.individual.NamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.HttpResponse
import utils.print.checkYourAnswers.TrusteePrintHelpers
import viewmodels.AnswerSection
import views.html.trustee.individual.amend.CheckDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val index = 0
  private val name = Name("Joe", None, "Bloggs")
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  private lazy val onPageLoadRoute: Call = routes.CheckDetailsController.onPageLoad(index)
  private lazy val onPageLoadUpdatedRoute: Call = routes.CheckDetailsController.onPageLoadUpdated(index)
  private lazy val onSubmitRoute: Call = routes.CheckDetailsController.onSubmit(index)

  private val trustee = TrusteeIndividual(
    name = name,
    dateOfBirth = None,
    phoneNumber = None,
    identification = None,
    address = None,
    countryOfResidence = None,
    nationality = None,
    mentalCapacityYesNo = None,
    entityStart = date,
    provisional = false
  )

  private val baseAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  private val mockPrintHelper = mock[TrusteePrintHelpers]
  private val mockMapper = mock[TrusteeMappers]
  private val answerSection = AnswerSection(None, Nil)

  "CheckDetails Controller" when {

    ".onPageLoad" must {

      "return OK and the correct view for a GET" in {

        val mockTrustService = mock[TrustService]

        when(mockTrustService.getTrustee(any(), any())(any(), any())).thenReturn(Future.successful(trustee))
        when(mockPrintHelper.printIndividualTrustee(any(), any(), any())(any())).thenReturn(answerSection)

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[TrustService].toInstance(mockTrustService),
            bind[TrusteePrintHelpers].toInstance(mockPrintHelper)
          ).build()

        val request = FakeRequest(GET, onPageLoadRoute.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckDetailsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection, index)(request, messages).toString
      }

      "return INTERNAL_SERVER_ERROR" when {
        "trustee is of type organisation" in {

          val mockTrustService = mock[TrustService]

          val trustee = TrusteeOrganisation(
            name = "Amazon",
            phoneNumber = None,
            email = None,
            identification = None,
            countryOfResidence = None,
            entityStart = date,
            provisional = false
          )

          when(mockTrustService.getTrustee(any(), any())(any(), any())).thenReturn(Future.successful(trustee))

          val application = applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          val request = FakeRequest(GET, onPageLoadRoute.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }

        "error getting trustee" in {

          val mockTrustService = mock[TrustService]

          when(mockTrustService.getTrustee(any(), any())(any(), any())).thenReturn(Future.failed(new Throwable("")))

          val application = applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          val request = FakeRequest(GET, onPageLoadRoute.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }

    ".onPageLoadUpdated" must {

      "return OK and the correct view for a GET" in {

        when(mockPrintHelper.printIndividualTrustee(any(), any(), any())(any())).thenReturn(answerSection)

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrusteePrintHelpers].toInstance(mockPrintHelper))
          .build()

        val request = FakeRequest(GET, onPageLoadUpdatedRoute.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckDetailsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection, index)(request, messages).toString
      }
    }

    ".onSubmit" when {

      "mapper returns trustee" must {
        "redirect to the the next page" in {

          val mockTrustConnector = mock[TrustConnector]

          when(mockMapper.mapToTrusteeIndividual(any())).thenReturn(Some(trustee))
          when(mockTrustConnector.amendTrustee(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

          val application = applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(
              bind[TrustConnector].toInstance(mockTrustConnector),
              bind[TrusteeMappers].toInstance(mockMapper)
            ).build()

          val request = FakeRequest(POST, onSubmitRoute.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

          verify(mockTrustConnector).amendTrustee(any(), eqTo(index), eqTo(trustee))(any(), any())
        }
      }

      "mapper returns None" must {

        "return InternalServerError" in {

          when(mockMapper.mapToTrusteeIndividual(any())).thenReturn(None)

          val application = applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[TrusteeMappers].toInstance(mockMapper))
            .build()

          val request = FakeRequest(POST, onSubmitRoute.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}

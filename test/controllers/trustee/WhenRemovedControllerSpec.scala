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

package controllers.trustee

import base.SpecBase
import connectors.TrustConnector
import forms.DateRemovedFromTrustFormProvider
import models.Constants.{BUSINESS_TRUSTEE, INDIVIDUAL_TRUSTEE}
import models.{Name, NationalInsuranceNumber, RemoveTrustee, TrusteeIndividual, TrusteeOrganisation, Trustees}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{TrustService, TrustServiceImpl}
import uk.gov.hmrc.http.HttpResponse
import views.html.trustee.WhenRemovedView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class WhenRemovedControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val formProvider = new DateRemovedFromTrustFormProvider()

  private def form: Form[LocalDate] = formProvider.withPrefixAndEntityStartDate("trustee.whenRemoved", LocalDate.now())

  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  val index = 0

  lazy val name: Name = Name(firstName = "First", middleName = None, lastName = "Last")

  val mockConnector: TrustConnector = mock[TrustConnector]

  val fakeService = new TrustServiceImpl(mockConnector)

  def dateRemovedFromTrustRoute(index: Int): String = routes.WhenRemovedController.onPageLoad(index).url

  def getRequest(index: Int): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateRemovedFromTrustRoute(index))

  def postRequest(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateRemovedFromTrustRoute(index))
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  def trusteeInd: TrusteeIndividual = TrusteeIndividual(
    name = name,
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    phoneNumber = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  def trusteeOrg: TrusteeOrganisation = TrusteeOrganisation(
    name = "Business",
    phoneNumber = None,
    email = None,
    identification = None,
    countryOfResidence = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  val trustees = List(trusteeInd, trusteeOrg)

  override def beforeEach(): Unit = {
    reset(mockConnector)

    when(mockConnector.getTrustees(any())(any(), any()))
      .thenReturn(Future.successful(Trustees(trustees)))

    when(mockConnector.removeTrustee(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(200, "")))
  }

  "WhenRemoved Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val result = route(application, getRequest(index)).value

      val view = application.injector.instanceOf[WhenRemovedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, name.displayName)(getRequest(index), messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" when {

      "individual" in {

        val index = 0

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustService].toInstance(fakeService))
          .build()

        val result = route(application, postRequest(index)).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[RemoveTrustee])
        verify(mockConnector).removeTrustee(any(), captor.capture)(any(), any())
        captor.getValue.`type` mustBe INDIVIDUAL_TRUSTEE

        application.stop()
      }

      "business" in {

        val index = 1

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustService].toInstance(fakeService))
          .build()

        val result = route(application, postRequest(index)).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[RemoveTrustee])
        verify(mockConnector).removeTrustee(any(), captor.capture)(any(), any())
        captor.getValue.`type` mustBe BUSINESS_TRUSTEE

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(POST, dateRemovedFromTrustRoute(index))
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[WhenRemovedView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, name.displayName)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, getRequest(index)).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, postRequest(index)).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}

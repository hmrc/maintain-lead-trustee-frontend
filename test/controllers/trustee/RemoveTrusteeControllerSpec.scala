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

package controllers.trustee

import base.SpecBase
import connectors.TrustConnector
import forms.RemoveIndexFormProvider
import models.Constants.INDIVIDUAL_TRUSTEE
import models.{Name, NationalInsuranceNumber, RemoveTrustee, TrusteeIndividual, Trustees}
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.RemoveIndexView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveTrusteeControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures with BeforeAndAfterEach {

  val messagesPrefix = "removeATrusteeYesNo"

  lazy val formProvider = new RemoveIndexFormProvider()
  lazy val form: Form[Boolean] = formProvider(messagesPrefix)

  lazy val formRoute: Call = routes.RemoveTrusteeController.onSubmit(0)

  lazy val content : String = "First 1 Last 1"

  val mockConnector: TrustConnector = Mockito.mock(classOf[TrustConnector])

  def trusteeInd(id: Int, provisional: Boolean): TrusteeIndividual = TrusteeIndividual(
    name = Name(firstName = s"First $id", middleName = None, lastName = s"Last $id"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    phoneNumber = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = provisional
  )

  val trustees = List(
    trusteeInd(1, provisional = false),
    trusteeInd(2, provisional = true),
    trusteeInd(3, provisional = true)
  )

  override def beforeEach(): Unit = {
    reset(mockConnector)

    when(mockConnector.getTrustees(any())(any(), any()))
      .thenReturn(Future.successful(Trustees(trustees)))

    when(mockConnector.removeTrustee(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(200, "")))
  }

  "RemoveTrustee Controller" when {

    "return OK and the correct view for a GET" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveTrusteeController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveIndexView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(messagesPrefix, form, index, content, formRoute)(request, messages).toString

      application.stop()
    }

    "not removing the trustee" must {

      "redirect to the add to page when valid data is submitted" in {

        val index = 0

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(POST, routes.RemoveTrusteeController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

        application.stop()
      }

      "show error page if we get an Index Not Found Exception" in {

        val index = 0

        when(mockConnector.getTrustees(any())(any(), any()))
          .thenReturn(Future.failed(new IndexOutOfBoundsException("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(GET, routes.RemoveTrusteeController.onPageLoad(index).url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

    "removing an existing trustee" must {

      "redirect to the next page when valid data is submitted" in {

        val index = 0

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(POST, routes.RemoveTrusteeController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.trustee.routes.WhenRemovedController.onPageLoad(0).url

        application.stop()
      }
    }

    "removing a new trustee" must {

      "redirect to the add to page, removing the trustee" in {

        val index = 2

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(POST, routes.RemoveTrusteeController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[RemoveTrustee])
        verify(mockConnector).removeTrustee(any(), captor.capture)(any(), any())
        captor.getValue.`type` mustBe INDIVIDUAL_TRUSTEE

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(POST, routes.RemoveTrusteeController.onSubmit(index).url)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveIndexView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(messagesPrefix, boundForm, index, content, formRoute)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveTrusteeController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.RemoveTrusteeController.onSubmit(index).url)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
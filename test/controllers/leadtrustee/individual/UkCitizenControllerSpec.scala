/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import forms.YesNoFormProvider
import models.BpMatchStatus.FullyMatched
import models.{Name, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.leadtrustee.individual.{BpMatchStatusPage, NamePage, NationalInsuranceNumberPage, UkCitizenPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import views.html.leadtrustee.individual.UkCitizenView

import scala.concurrent.Future

class UkCitizenControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new YesNoFormProvider()
  val form: Form[Boolean] = formProvider.withPrefix("leadtrustee.individual.ukCitizen")

  val name: Name = Name("Lead", None, "Trustee")

  lazy val ukCitizenRoute: String = routes.UkCitizenController.onPageLoad().url

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .set(NamePage, name).success.value

  "UkCitizen Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, ukCitizenRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UkCitizenView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, name.displayName, readOnly = false)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET" when {
      "question has previously been answered" when {

        "lead trustee not matched" in {

          val userAnswers = emptyUserAnswers.set(UkCitizenPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, ukCitizenRoute)

          val view = application.injector.instanceOf[UkCitizenView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(true), name.displayName, readOnly = false)(request, messages).toString

          application.stop()
        }

        "lead trustee matched" in {

          val userAnswers = emptyUserAnswers
            .set(UkCitizenPage, true).success.value
            .set(NationalInsuranceNumberPage, "nino").success.value
            .set(BpMatchStatusPage, FullyMatched).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, ukCitizenRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UkCitizenView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(true), name.displayName, readOnly = true)(request, messages).toString

          application.stop()
        }
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      val request =
        FakeRequest(POST, ukCitizenRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, ukCitizenRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[UkCitizenView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name.displayName, readOnly = false)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, ukCitizenRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, ukCitizenRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}

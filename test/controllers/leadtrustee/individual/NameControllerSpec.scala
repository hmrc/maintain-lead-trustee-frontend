/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.IndividualNameFormProvider
import models.BpMatchStatus.FullyMatched
import models.{Name, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.Mockito
import pages.leadtrustee.individual.{BpMatchStatusPage, NamePage, NationalInsuranceNumberPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import views.html.leadtrustee.individual.NameView

import scala.concurrent.Future

class NameControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new IndividualNameFormProvider()
  val form: Form[Name] = formProvider.withPrefix("leadtrustee.individual.name")

  lazy val nameRoute: String = routes.NameController.onPageLoad().url

  val name: Name = Name("Lead", None, "Trustee")

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  "Name Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, nameRoute)

      val view = application.injector.instanceOf[NameView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, readOnly = false)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET" when {
      "question has previously been answered" when {

        "lead trustee not matched" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, nameRoute)

          val view = application.injector.instanceOf[NameView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(name), readOnly = false)(request, messages).toString

          application.stop()
        }

        "lead trustee matched" in {

          val userAnswers = baseAnswers
            .set(NationalInsuranceNumberPage, "nino").success.value
            .set(BpMatchStatusPage, FullyMatched).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, nameRoute)

          val view = application.injector.instanceOf[NameView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(name), readOnly = true)(request, messages).toString

          application.stop()
        }
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = Mockito.mock(classOf[PlaybackRepository])

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()


      val request =
        FakeRequest(POST, nameRoute)
          .withFormUrlEncodedBody(("firstName", "Peter"), ("lastName", "Pan"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, nameRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[NameView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, readOnly = false)(request, messages).toString

       application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, nameRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, nameRoute)
          .withFormUrlEncodedBody(("firstName", "value 1"), ("lastName", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}

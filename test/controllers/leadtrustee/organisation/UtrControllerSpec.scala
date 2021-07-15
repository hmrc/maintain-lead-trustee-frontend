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

package controllers.leadtrustee.organisation

import base.SpecBase
import forms.UtrFormProvider
import navigation.Navigator
import pages.leadtrustee.organisation.{NamePage, UtrPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import repositories.PlaybackRepository
import views.html.leadtrustee.organisation.UtrView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.inject.bind
import services.TrustServiceImpl

import scala.concurrent.Future

class UtrControllerSpec extends SpecBase {

  val formProvider = new UtrFormProvider()
  val form: Form[String] = formProvider.apply("leadtrustee.organisation.utr", "utr", Nil)

  val index = 0
  val fakeBusinessName = "Business name"
  val fakeUtr = "1234567890"

  lazy val trusteeUtrRoute: String = routes.UtrController.onPageLoad().url

  val mockTrustsService: TrustServiceImpl = mock[TrustServiceImpl]
  when(mockTrustsService.getBusinessLeadTrusteeUtr(any())(any(), any()))
    .thenReturn(Future.successful(Nil))

  "TrusteeUtr Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, fakeBusinessName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustServiceImpl].toInstance(mockTrustsService))
        .build()

      val request = FakeRequest(GET, trusteeUtrRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[UtrView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, fakeBusinessName)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, fakeBusinessName).success.value
        .set(UtrPage, fakeUtr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustServiceImpl].toInstance(mockTrustsService))
        .build()

      val request = FakeRequest(GET, trusteeUtrRoute)

      val view = application.injector.instanceOf[UtrView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(fakeUtr), fakeBusinessName)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(fakeNavigator),
          bind[TrustServiceImpl].toInstance(mockTrustsService)
        ).build()

      val request = FakeRequest(POST, trusteeUtrRoute)
        .withFormUrlEncodedBody(("value", fakeUtr))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, fakeBusinessName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustServiceImpl].toInstance(mockTrustsService))
        .build()

      val request = FakeRequest(POST, trusteeUtrRoute)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[UtrView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, fakeBusinessName)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, trusteeUtrRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, trusteeUtrRoute)
        .withFormUrlEncodedBody(("value", fakeUtr))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}

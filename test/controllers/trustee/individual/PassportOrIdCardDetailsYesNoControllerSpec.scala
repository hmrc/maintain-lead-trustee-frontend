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

package controllers.trustee.individual

import base.SpecBase
import models.{Mode, Name, NormalMode}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.Mockito
import pages.trustee.individual.amend.IndexPage
import pages.trustee.individual.{NamePage, PassportOrIdCardDetailsYesNoPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository

import scala.concurrent.Future

class PassportOrIdCardDetailsYesNoControllerSpec extends SpecBase {

  private val name = Name("FirstName", None, "LastName")
  private val mode: Mode = NormalMode
  private val index = 0


  val userAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(IndexPage, index).success.value

  lazy val passportOrIdCardDetailsYesNoRoute = routes.PassportOrIdCardDetailsYesNoController.onPageLoad(mode).url

  private lazy val checkDetailsRoute =
    controllers.trustee.individual.amend.routes.CheckDetailsController.onPageLoadUpdated(index).url

  "PassportOrIdCardDetailsYesNo Controller" must {

    "redirect to check details for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to check details when previously answered" in {

      val newUserAnswers = userAnswers.set(PassportOrIdCardDetailsYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(newUserAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to the check details when valid data is submitted" in {

      val mockPlaybackRepository = Mockito.mock(classOf[PlaybackRepository])

      when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[Navigator].toInstance(fakeNavigator))
        .build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}

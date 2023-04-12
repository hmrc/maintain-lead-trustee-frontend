/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{Name, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.leadtrustee.individual.{NamePage, NationalInsuranceNumberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.leadtrustee.individual.MatchingLockedView

import scala.concurrent.Future

class MatchingLockedControllerSpec extends SpecBase {

  private val name: Name = Name("Joe", None, "Bloggs")

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(NationalInsuranceNumberPage, "AA000000A").success.value

  "MatchingLockedController" when {

    ".onPageLoad" when {

      lazy val onPageLoadRoute = routes.MatchingLockedController.onPageLoad().url

      "existing data found" must {
        "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MatchingLockedView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(name.displayFullName)(request, messages).toString

          application.stop()
        }
      }

      "no existing data found" must {
        "redirect to Session Expired" in {

          val application = applicationBuilder(userAnswers = None).build()

          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

          application.stop()
        }
      }
    }

    ".continue" when {

      lazy val continueWithPassportRoute = routes.MatchingLockedController.continue().url

      "existing data found" must {

        "amend user answers and redirect to passport page" in {

          reset(playbackRepository)
          when(playbackRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, continueWithPassportRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.PassportOrIdCardController.onPageLoad().url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(playbackRepository).set(uaCaptor.capture)

          uaCaptor.getValue.get(NationalInsuranceNumberPage) mustBe None

          application.stop()
        }
      }

      "no existing data found" must {
        "redirect to Session Expired" in {

          val application = applicationBuilder(userAnswers = None).build()

          val request = FakeRequest(GET, continueWithPassportRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

          application.stop()
        }
      }
    }

  }
}

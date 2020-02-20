/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.YesNoFormProvider
import forms.trustee.AddATrusteeFormProvider
import models.{AddATrustee, Name, NormalMode}
import models.IndividualOrBusiness
import models.Status.Completed
import pages.trustee.{IndividualOrBusinessPage, TrusteeStatus}
import pages.trustee.individual.NamePage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.addAnother.AddRow
import views.html.trustee.{AddATrusteeView, AddATrusteeYesNoView}

class AddATrusteeControllerSpec extends SpecBase {

  lazy val getRoute : String = routes.AddATrusteeController.onPageLoad().url
  lazy val submitAnotherRoute : String = routes.AddATrusteeController.submitAnother().url
  lazy val submitYesNoRoute : String = routes.AddATrusteeController.submitOne().url

  val addTrusteeForm = new AddATrusteeFormProvider()()
  val yesNoForm = new YesNoFormProvider().withPrefix("addATrusteeYesNo")

  val trustee = List(
    AddRow("First 0 Last 0", typeLabel = "Trustee Individual", "#", "/maintain-a-trust/trustees/trustee/0/remove"),
    AddRow("First 1 Last 1", typeLabel = "Trustee Individual", "#", "/maintain-a-trust/trustees/trustee/1/remove")
  )

  val userAnswersWithTrusteesComplete = emptyUserAnswers
    .set(IndividualOrBusinessPage(0), IndividualOrBusiness.Individual).success.value
    .set(NamePage(0), Name("First 0", None, "Last 0")).success.value
    .set(TrusteeStatus(0), Completed).success.value
    .set(IndividualOrBusinessPage(1), IndividualOrBusiness.Individual).success.value
    .set(NamePage(1), Name("First 1", None, "Last 1")).success.value
    .set(TrusteeStatus(1), Completed).success.value

  def onwardRoute = Call("GET", "/maintain-a-trust/trustees")

  "AddATrustee Controller" when {

    "no data" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddATrustee.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }

    "no trustees" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddATrusteeYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(yesNoForm, NormalMode)(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(POST, submitYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.leadtrustee.individual.routes.NameController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(POST, submitYesNoRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = yesNoForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddATrusteeYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(fakeRequest, messages).toString

        application.stop()
      }
    }

    "there are trustees" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithTrusteesComplete)).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddATrusteeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addTrusteeForm, NormalMode ,Nil, trustee, isLeadTrusteeDefined = false, heading = "You have added 2 trustees")(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithTrusteesComplete)).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddATrustee.options.head.value))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithTrusteesComplete)).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addTrusteeForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddATrusteeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            Nil,
            trustee,
            isLeadTrusteeDefined = false,
            heading = "You have added 2 trustees"
          )(fakeRequest, messages).toString

        application.stop()
      }

    }

  }
}

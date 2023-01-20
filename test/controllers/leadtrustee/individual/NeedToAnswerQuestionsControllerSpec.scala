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
import models.Name
import org.mockito.MockitoSugar
import pages.leadtrustee.individual.NamePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.leadtrustee.individual.NeedToAnswerQuestionsView

class NeedToAnswerQuestionsControllerSpec extends SpecBase with MockitoSugar {

  val leadTrustee: Name = Name("Lead", None, "Trustee")

  "NeedToAnswerQuestions Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(NamePage, leadTrustee).success.value)).build()

      val request = FakeRequest(GET, routes.NeedToAnswerQuestionsController.onPageLoad.url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[NeedToAnswerQuestionsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(leadTrustee.displayName)(request, messages).toString

      application.stop()
    }
  }
}

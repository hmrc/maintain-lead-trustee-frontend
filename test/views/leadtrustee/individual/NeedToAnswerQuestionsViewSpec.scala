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

package views.leadtrustee.individual

import models.Name
import views.behaviours.ViewBehaviours
import views.html.leadtrustee.individual.NeedToAnswerQuestionsView

class NeedToAnswerQuestionsViewSpec extends ViewBehaviours {

  "NeedToAnswerQuestions view" must {

    val leadTrustee = Name("Lead", None, "Trustee")

    val messageKeyPrefix = "answerNewQuestions"

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[NeedToAnswerQuestionsView]

    val applyView = view.apply(leadTrustee.displayName)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView, messageKeyPrefix, leadTrustee.displayName)

    behave like pageWithBackLink(applyView)

    behave like pageWithDynamicHint(applyView, messageKeyPrefix, leadTrustee.displayName)

    behave like pageWithContinueButton(applyView, controllers.leadtrustee.individual.routes.NameController.onPageLoad().url)
  }
}

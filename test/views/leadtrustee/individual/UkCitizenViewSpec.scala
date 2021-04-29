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

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.leadtrustee.individual.UkCitizenView

class UkCitizenViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "leadtrustee.individual.ukCitizen"

  val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

  val name = "Lead Trustee"

  "UkCitizenView" when {

    "not read-only" must {

      val view = viewFor[UkCitizenView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, name, readOnly = false)(fakeRequest, messages)

      behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

      behave like pageWithBackLink(applyView(form))

      behave like yesNoPage(form, applyView, messageKeyPrefix, Some(name), controllers.leadtrustee.individual.routes.UkCitizenController.onSubmit().url)

      behave like pageWithoutDisabledInput(applyView(form))
    }

    "read-only" must {

      val view = viewFor[UkCitizenView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, name, readOnly = true)(fakeRequest, messages)

      behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

      behave like pageWithBackLink(applyView(form))

      behave like yesNoPage(form, applyView, messageKeyPrefix, Some(name), controllers.leadtrustee.individual.routes.UkCitizenController.onSubmit().url)

      behave like pageWithDisabledInput(applyView(form))
    }
  }
}

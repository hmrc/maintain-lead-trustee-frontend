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

package views.trustee.individual

import forms.YesNoFormProvider
import models.{Name, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.trustee.individual.CountryOfNationalityInTheUkYesNoView
import controllers.trustee.individual.routes

class CountryOfNationalityInTheUkYesNoViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "trustee.individual.countryOfNationalityInTheUkYesNo"
  val name: Name = Name("First", None, "Last")

  val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

  "countryOfNationalityInTheUkYesNoView" must {

    val view = viewFor[CountryOfNationalityInTheUkYesNoView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, name.displayName)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name.displayName)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, Some(name.displayName), routes.CountryOfNationalityInTheUkYesNoController.onSubmit(NormalMode).url)

    behave like pageWithASubmitButton(applyView(form))
  }
}

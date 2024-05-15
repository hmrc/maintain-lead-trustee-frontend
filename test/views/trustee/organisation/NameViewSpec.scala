/*
 * Copyright 2024 HM Revenue & Customs
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

package views.trustee.organisation

import controllers.trustee.individual.routes
import forms.BusinessNameFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.trustee.organisation.NameView

class NameViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "trustee.organisation.name"

  val form = new BusinessNameFormProvider().withPrefix("trustee.organisation.name")
  val view = viewFor[NameView](Some(emptyUserAnswers))

  "Name view" must {

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithTextFields(
      form,
      applyView,
      messageKeyPrefix,
      None,
      routes.NameController.onSubmit(NormalMode).url,
      "value"
    )

    behave like pageWithASubmitButton(applyView(form))
  }

}

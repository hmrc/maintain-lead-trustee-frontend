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

package views.trustee.individual

import controllers.trustee.individual.routes
import forms.IndividualNameFormProvider
import models.{Name, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.trustee.individual.NameView

class NameViewSpec extends QuestionViewBehaviours[Name] {

  val messageKeyPrefix = "trustee.individual.name"
  val name: Name = Name("First", Some("Middle"), "Last")

  override val form: Form[Name] = new IndividualNameFormProvider().withPrefix(messageKeyPrefix)

  "Name view" must {

    val view = viewFor[NameView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    "fields" must {

      behave like pageWithTextFields(
        form,
        applyView,
        messageKeyPrefix,
        None,
        routes.NameController.onSubmit(NormalMode).url,
        "firstName",
        "middleName",
        "lastName"
      )
    }

    behave like pageWithASubmitButton(applyView(form))
  }
}

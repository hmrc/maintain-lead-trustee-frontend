/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.leadtrustee.individual.routes
import forms.IndividualNameFormProvider
import models.Name
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.leadtrustee.individual.NameView

class NameViewSpec extends QuestionViewBehaviours[Name] {

  val messageKeyPrefix = "leadtrustee.individual.name"

  override val form: Form[Name] = new IndividualNameFormProvider().withPrefix(messageKeyPrefix)

  "NameView" when {

    "not read-only" must {

      val view = viewFor[NameView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, readOnly = false)(fakeRequest, messages)


      behave like normalPage(applyView(form), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form))

      behave like pageWithTextFields(
        form,
        applyView,
        messageKeyPrefix,
        None,
        routes.NameController.onSubmit().url,
        "firstName", "lastName"
      )

      behave like pageWithoutReadOnlyInput(applyView(form))
    }

    "read-only" must {

      val view = viewFor[NameView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, readOnly = true)(fakeRequest, messages)


      behave like normalPage(applyView(form), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form))

      behave like pageWithTextFields(
        form,
        applyView,
        messageKeyPrefix,
        None,
        routes.NameController.onSubmit().url,
        "firstName", "lastName"
      )

      behave like pageWithReadOnlyInput(applyView(form))
    }
  }
}

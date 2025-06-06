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

package views.leadtrustee.organisation

import forms.BusinessNameFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.leadtrustee.organisation.NameView

class NameViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "leadtrustee.organisation.name"

  val form = new BusinessNameFormProvider().withPrefix("leadtrustee.organisation.name")
  val view = viewFor[NameView](Some(emptyUserAnswers))

  "TrusteeBusinessName view" must {

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, true)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithTextFields(
      form,
      applyView,
      messageKeyPrefix,
      None,
      controllers.leadtrustee.organisation.routes.NameController.onSubmit().url,
      "value"
    )

    behave like pageWithASubmitButton(applyView(form))
  }

  "Name view for a UK registered company" must {

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, true)(fakeRequest, messages)

      "display hint text" in {
        val doc = asDocument(applyView(form))
        assertContainsText(doc, messages(s"$messageKeyPrefix.hint"))
      }
  }

  "Name view for a non-UK registered company" must {

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, false)(fakeRequest, messages)

    "hint text not displayed" in {
      val doc = asDocument(applyView(form))
        assertNotRenderedByClass(doc, "form-hint")
    }
  }

}

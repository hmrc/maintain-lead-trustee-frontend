/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import forms.ReplaceLeadTrusteeFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.RadioOption
import views.behaviours.StringViewBehaviours
import views.html.ReplacingLeadTrusteeView

class ReplacingLeadTrusteeViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "replacingLeadTrustee"

  val form = new ReplaceLeadTrusteeFormProvider().withPrefix(messageKeyPrefix)

  val view = viewFor[ReplacingLeadTrusteeView](Some(emptyUserAnswers))

  def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, name, radioOptions)(fakeRequest, messages)

  val name = "Lead Trustee"

  val radioOptions = List(RadioOption(s"$messageKeyPrefix.-1", "-1", s"$messageKeyPrefix.add-new"))

  "ReplacingLeadTrustee view" must {

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))

  }

  for (option <- radioOptions)

    s"rendered with a value of '${option.value}'" must {

      s"have the '${option.value}' radio button selected" in {

        val doc = asDocument(applyView(form.bind(Map("value" -> s"${option.value}"))))

        assertContainsRadioButton(doc, option.id, "value", option.value, isChecked = true)

        for (unselectedOption <- radioOptions.filterNot(o => o == option))
          assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, isChecked = false)
      }
    }

}

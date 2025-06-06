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

import controllers.leadtrustee.organisation.routes
import forms.TelephoneNumberFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.leadtrustee.organisation.TelephoneNumberView

class TelephoneNumberViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "leadtrustee.organisation.telephoneNumber"

  val form: Form[String] = new TelephoneNumberFormProvider().withPrefix(messageKeyPrefix)

  val name = "Lead Trustee"

  "TelephoneNumberView view" must {

    val view = viewFor[TelephoneNumberView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, name)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView(form))

    behave like stringPage(form, applyView, messageKeyPrefix, Some(name), routes.TelephoneNumberController.onSubmit().url)

    "display hint text" in {
      val doc = asDocument(applyView(form))
      assertContainsText(doc, messages(s"site.telephone_number.hint"))
    }
  }
}

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

package views.leadtrustee.individual

import controllers.leadtrustee.individual.routes
import forms.NonUkAddressFormProvider
import models.NonUkAddress
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.leadtrustee.individual.NonUkAddressView

class NonUkAddressViewSpec extends QuestionViewBehaviours[NonUkAddress] {

  val messageKeyPrefix = "leadtrustee.individual.nonUkAddress"

  override val form = new NonUkAddressFormProvider()()

  "leadtrustee.individual.NonUkAddressView" must {

    val view = viewFor[NonUkAddressView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, Seq.empty, "Lead Trustee")(fakeRequest, messages)


    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, "Lead Trustee")

    behave like pageWithBackLink(applyView(form))

    behave like pageWithTextFields(
      form,
      applyView,
      messageKeyPrefix,
      Some("Lead Trustee"),
      routes.NonUkAddressController.onSubmit().url,
      "line1", "line2"
    )
  }
}

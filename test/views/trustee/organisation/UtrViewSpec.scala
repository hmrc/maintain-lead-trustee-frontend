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

import controllers.trustee.organisation.routes
import forms.UtrFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.trustee.organisation.UtrView

class UtrViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "trustee.organisation.utr"
  val name = "Trustee Name"

  val form: Form[String] = new UtrFormProvider().apply(messageKeyPrefix, "utr", Nil)

  "TrusteeUtrView view" must {

    val view = viewFor[UtrView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, name)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like stringPage(form, applyView, messageKeyPrefix, Some(name), routes.UtrController.onSubmit(NormalMode).url)

    behave like pageWithBackLink(applyView(form))
  }
}

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

package views.trustee

import forms.IndividualOrBusinessFormProvider
import models.IndividualOrBusiness
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.OptionsViewBehaviours
import views.html.trustee.IndividualOrBusinessView

class IndividualOrBusinessViewSpec extends OptionsViewBehaviours {

  val messageKeyPrefix = "trustee.individualOrBusiness"

  val form = new IndividualOrBusinessFormProvider().withPrefix(messageKeyPrefix)

  "IndividualOrBusinessView" must {

    val view = viewFor[IndividualOrBusinessView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithOptions(form, applyView, IndividualOrBusiness.options)
  }
}

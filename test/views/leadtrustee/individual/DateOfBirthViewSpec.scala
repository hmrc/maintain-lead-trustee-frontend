/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import forms.DateOfBirthFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.leadtrustee.individual.DateOfBirthView

class DateOfBirthViewSpec extends QuestionViewBehaviours[LocalDate] {

  val messageKeyPrefix = "leadtrustee.individual.dateOfBirth"

  val form = new DateOfBirthFormProvider().withPrefix("leadtrustee.individual")

  "DateOfBirthView view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[DateOfBirthView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, "LeadTrusteeName")(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, "LeadTrusteeName")

    behave like pageWithBackLink(applyView(form))
  }
}

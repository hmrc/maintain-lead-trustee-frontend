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

package views.leadtrustee

import play.twirl.api.HtmlFormat
import views.behaviours.OptionsViewBehaviours
import views.html.leadtrustee.UnableToRemoveView

class UnableToRemoveViewSpec extends OptionsViewBehaviours {

  val messageKeyPrefix = "unableToRemove"
  val name = "Test Name"

  "UnableToRemoveView" must {

    val view = viewFor[UnableToRemoveView](Some(emptyUserAnswers))

    def applyView(): HtmlFormat.Appendable =
      view.apply(name)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView())

  }
}

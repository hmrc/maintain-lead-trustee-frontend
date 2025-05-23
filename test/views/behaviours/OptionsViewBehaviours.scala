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

package views.behaviours

import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.RadioOption

trait OptionsViewBehaviours extends ViewBehaviours {

  def pageWithOptions[T](form : Form[T],
                      createView: Form[T] => HtmlFormat.Appendable,
                      options : List[RadioOption]) : Unit = {

    "behave like a page with radio options" when {

      "rendered" must {

        "contain radio buttons for the values" in {

          val doc = asDocument(createView(form))

          for (option <- options) {
            assertContainsRadioButton(doc, option.id, "value", option.value, false)
          }
        }
      }

      for (option <- options) {

        s"rendered with a value of '${option.value}'" must {

          s"have the '${option.value}' radio button selected" in {

            val doc = asDocument(createView(form.bind(Map("value" -> s"${option.value}"))))

            assertContainsRadioButton(doc, option.id, "value", option.value, true)

            for (unselectedOption <- options.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, false)
            }
          }
        }
      }
    }


  }

}

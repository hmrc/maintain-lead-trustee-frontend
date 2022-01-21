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

package views.behaviours

import play.twirl.api.HtmlFormat
import viewmodels.Link

trait LinkListViewBehaviours extends ViewBehaviours {

  def linkList(view: HtmlFormat.Appendable,
               expectedLinks: List[Link]): Unit = {

    "behave list a page with a link list" when {
      "rendered" must {

        expectedLinks.foreach { link =>
          s"${link.text}" must {

            s"render a list item" in {
              val doc = asDocument(view)
              assertRenderedById(doc, s"link-list__item--${link.cssText}")
            }

            s"render a link" in {
              val id = s"link-list__link--${link.cssText}"

              val doc = asDocument(view)
              doc.getElementById(id).hasAttr("href")
            }
          }
        }
      }
    }
  }
}

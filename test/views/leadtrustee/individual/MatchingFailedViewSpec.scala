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

package views.leadtrustee.individual

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.leadtrustee.individual.MatchingFailedView

class MatchingFailedViewSpec extends ViewBehaviours {

  val prefix = "leadTrustee.individual.matching.failed"
  val index = 0
  val view: MatchingFailedView = viewFor[MatchingFailedView](Some(emptyUserAnswers))

  "FailedMatching View" when {

    "1 attempt left" must {

      val numberOfFailedAttempts: Int = 2

      def applyView: HtmlFormat.Appendable =
        view.apply(numberOfFailedAttempts, frontendAppConfig.maxMatchingAttempts - numberOfFailedAttempts)(fakeRequest, messages)

      behave like normalPageTitleWithCaption(
        view = applyView,
        messageKeyPrefix = prefix,
        messageKeyParam = "",
        captionParam = numberOfFailedAttempts.toString,
        expectedGuidanceKeys = "paragraph1", "paragraph2.part1", "paragraph2.part2.singular"
      )

      "show number of remaining attempts in bold" in {
        val doc = asDocument(applyView)

        assertAttributeValueForElement(doc.getElementById("remaining-attempts"), "class", "govuk-!-font-weight-bold")
      }

      behave like pageWithASubmitButton(applyView)
    }

    "more than 1 attempt left" must {

      val numberOfFailedAttempts: Int = 1

      def applyView: HtmlFormat.Appendable =
        view.apply(numberOfFailedAttempts, frontendAppConfig.maxMatchingAttempts - numberOfFailedAttempts)(fakeRequest, messages)

      behave like normalPageTitleWithCaption(
        view = applyView,
        messageKeyPrefix = prefix,
        messageKeyParam = "",
        captionParam = numberOfFailedAttempts.toString,
        expectedGuidanceKeys = "paragraph1", "paragraph2.part1", "paragraph2.part2.plural"
      )

      "show number of remaining attempts in bold" in {
        val doc = asDocument(applyView)

        assertAttributeValueForElement(doc.getElementById("remaining-attempts"), "class", "govuk-!-font-weight-bold")
      }

      behave like pageWithASubmitButton(applyView)

    }
  }
}

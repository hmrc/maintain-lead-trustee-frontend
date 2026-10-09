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

package views.trustee

import play.twirl.api.HtmlFormat
import viewmodels.addAnother.{AddRow, TrusteeRows}
import views.behaviours.ViewBehaviours
import views.html.trustee.MaxedOutTrusteesView

class MaxedOutTrusteesViewSpec extends ViewBehaviours {

  private val view: MaxedOutTrusteesView = viewFor[MaxedOutTrusteesView](Some(emptyUserAnswers))

  private val leadTrusteeRow = AddRow(
    name = "Amina Doe",
    typeLabel = "Lead Trustee Individual",
    changeLabel = "Change details",
    changeUrl = "/maintain-a-trust/trustees/lead-trustee/individual/check-details",
    removeLabel = Some("Cannot remove"),
    removeUrl = None
  )

  private def trusteeRow(name: String, index: Int): AddRow = AddRow(
    name = name,
    typeLabel = "Trustee Individual",
    changeLabel = "Change details",
    changeUrl = s"/maintain-a-trust/trustees/trustee/individual/$index/check-details",
    removeLabel = Some("Remove"),
    removeUrl = Some(s"/maintain-a-trust/trustees/trustee/$index/remove")
  )

  private val rows = TrusteeRows(
    lead = Some(leadTrusteeRow),
    otherTrustees = Seq(trusteeRow("John Doe", 0)),
    lackingMentalCapacity = Seq(trusteeRow("James Smith", 1))
  )

  private val heading = messages("addATrustee.count.heading", 26)

  private val applyView: HtmlFormat.Appendable = view(rows, heading)(fakeRequest, messages)

  "MaxedOutTrustees view" must {

    behave like pageWithBackLink(applyView)

    behave like pageWithASubmitButton(applyView)

    "use the trustee count as the H1" in {
      val doc = asDocument(applyView)

      assertPageTitleEqualsMessage(doc, "addATrustee.count.heading", 26)
    }

    "show the same trustee summary as the add a trustee page, including trustees who lack mental capacity" in {
      val doc = asDocument(applyView)

      assertRenderedById(doc, "p--leadTrusteeContact")
      assertRenderedById(doc, "data-list--otherTrustees")
      assertRenderedById(doc, "heading--lackMentalCapacity")
    }

    "send 'change the lead trustee' through ChangeLeadTrusteeController, so users with no eligible trustees skip the radio page" in {
      val doc = asDocument(applyView)

      doc.getElementById("link--changeLeadTrustee").attr("href") mustBe
        controllers.routes.ChangeLeadTrusteeController.onPageLoad().url
    }

    "explain the maximum has been reached, without asking to add another trustee" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, "addATrustee.maxedOut", "addATrustee.maxedOut.paragraph")
      assertNotRenderedByCssSelector(doc, "input[type=radio]")
      doc.select("form").attr("action") mustBe controllers.routes.AddATrusteeController.submitComplete().url
    }
  }

}

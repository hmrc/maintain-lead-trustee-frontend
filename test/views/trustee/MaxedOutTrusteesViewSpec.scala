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

import scala.jdk.CollectionConverters._

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

  private val heading = "The trust has 26 trustees"

  private val applyView: HtmlFormat.Appendable = view(rows, heading)(fakeRequest, messages)

  "MaxedOutTrustees view" must {

    behave like pageWithBackLink(applyView)

    behave like pageWithASubmitButton(applyView)

    val doc = asDocument(applyView)

    "show the trustee count as the page title and H1" in {
      doc.title mustBe "The trust has 26 trustees - Trustees - Manage a trust - GOV.UK"

      doc.getElementsByTag("h1").asScala.map(_.text).toSeq mustBe Seq("The trust has 26 trustees")
    }

    "show the same trustee summary as the add a trustee page, including trustees who lack mental capacity" in {
      assertContainsTextForId(
        doc,
        "p--leadTrusteeContact",
        "This is the person or business HMRC will contact to discuss the trust or send official documents to. The lead trustee is responsible for keeping the trust’s details up to date."
      )

      assertContainsTextForId(doc, "data-list-heading--otherTrustees", "Other trustees")

      assertContainsTextForId(
        doc,
        "heading--lackMentalCapacity",
        "You believe that these trustees lack mental capacity"
      )
    }

    "send 'change the lead trustee' through ChangeLeadTrusteeController, so users with no eligible trustees skip the radio page" in {
      doc.getElementById("link--changeLeadTrustee").attr("href") mustBe
        controllers.routes.ChangeLeadTrusteeController.onPageLoad().url
    }

    "explain in an inset that the maximum has been reached" in {
      doc.select(".govuk-inset-text li").asScala.map(_.text).toSeq mustBe Seq(
        "You cannot add another trustee as you have entered a maximum of 26.",
        "You can add another trustee by removing an existing one, or write to HMRC with details of any additional trustees."
      )
    }

    "not ask to add another trustee, and submit to complete the trustees section" in {
      assertNotRenderedByCssSelector(doc, "input[type=radio]")
      doc.select("form").attr("action") mustBe controllers.routes.AddATrusteeController.submitComplete().url
    }
  }

}

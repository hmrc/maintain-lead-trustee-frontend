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

package views.components

import org.jsoup.nodes.Document
import viewmodels.addAnother.{AddRow, TrusteeRows}
import views.ViewSpecBase
import views.html.components.TrusteeSummary

import scala.jdk.CollectionConverters._

class TrusteeSummarySpec extends ViewSpecBase {

  private val component: TrusteeSummary = viewFor[TrusteeSummary](Some(emptyUserAnswers))

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

  private def render(rows: TrusteeRows): Document =
    asDocument(component(rows)(messages))

  private def namesIn(doc: Document, listId: String): Seq[String] =
    doc.select(s"#$listId .hmrc-add-to-a-list__identifier__first").asScala.map(_.text()).toSeq

  "TrusteeSummary component" when {

    "there is only a lead trustee" must {
      val doc = render(TrusteeRows(Some(leadTrusteeRow), Nil, Nil))

      "show the lead trustee with its contact paragraph and change link, but not 'All trustees have the same legal responsibilities' as there is only one trustee" in {
        assertNotRenderedById(doc, "p--allTrusteesSameResponsibilities")

        namesIn(doc, "data-list--leadTrustee")                     mustBe Seq("Amina Doe")
        assertRenderedById(doc, "p--leadTrusteeContact")
        doc.getElementById("link--changeLeadTrustee").attr("href") mustBe
          controllers.routes.ChangeLeadTrusteeController.onPageLoad().url
      }

      "not show other trustees, the lack mental capacity section or the lead trustee required inset" in {
        assertNotRenderedById(doc, "data-list-heading--otherTrustees")
        assertNotRenderedById(doc, "heading--lackMentalCapacity")
        assertNotRenderedById(doc, "inset-text--addATrustee")
      }
    }

    "there is a lead trustee and other trustees who all have mental capacity" must {
      val doc = render(TrusteeRows(Some(leadTrusteeRow), Seq(trusteeRow("John Doe", 0)), Nil))

      "show 'All trustees have the same legal responsibilities' as there is more than one trustee" in
        assertRenderedById(doc, "p--allTrusteesSameResponsibilities")

      "list the other trustees, without the lack mental capacity section" in {
        assertRenderedById(doc, "data-list-heading--otherTrustees")
        namesIn(doc, "data-list--otherTrustees") mustBe Seq("John Doe")

        assertNotRenderedById(doc, "heading--lackMentalCapacity")
        assertNotRenderedById(doc, "data-list--lackMentalCapacity")
        assertNotRenderedById(doc, "p--lackMentalCapacity-1")
        assertNotRenderedById(doc, "p--lackMentalCapacity-2")
      }
    }

    "some other trustees lack mental capacity" must {
      val rows = TrusteeRows(
        lead = Some(leadTrusteeRow),
        otherTrustees = Seq(trusteeRow("John Doe", 0), trusteeRow("George Wilson", 1)),
        lackingMentalCapacity = Seq(trusteeRow("James Smith", 2))
      )
      val doc  = render(rows)

      "list only trustees with mental capacity under other trustees" in {
        namesIn(doc, "data-list--otherTrustees") mustBe Seq("John Doe", "George Wilson")
      }

      "list trustees who lack mental capacity under their own H3, after the other trustees list" in {
        doc.getElementById("heading--lackMentalCapacity").tagName() mustBe "h3"

        namesIn(doc, "data-list--lackMentalCapacity") mustBe Seq("James Smith")

        val ids = doc.select("[id]").asScala.map(_.id()).toSeq
        ids.indexOf("heading--lackMentalCapacity") must be > ids.indexOf("data-list--otherTrustees")
      }

      "keep change and remove links for trustees who lack mental capacity, so their details can still be updated" in {
        val links = doc.select("#data-list--lackMentalCapacity a").asScala.map(_.attr("href")).toSeq

        links mustBe Seq(
          "/maintain-a-trust/trustees/trustee/individual/2/check-details",
          "/maintain-a-trust/trustees/trustee/2/remove"
        )
      }

      "show both paragraphs explaining mental capacity" in {
        assertRenderedById(doc, "p--lackMentalCapacity-1")
        assertRenderedById(doc, "p--lackMentalCapacity-2")
      }
    }

    "every trustee other than the lead lacks mental capacity" must {
      val doc = render(TrusteeRows(Some(leadTrusteeRow), Nil, Seq(trusteeRow("James Smith", 0))))

      "still show the other trustees heading, as the lack mental capacity section sits within it" in {
        assertRenderedById(doc, "data-list-heading--otherTrustees")
        assertNotRenderedById(doc, "data-list--otherTrustees")

        namesIn(doc, "data-list--lackMentalCapacity") mustBe Seq("James Smith")
      }
    }

    "there is no lead trustee" must {
      val doc = render(TrusteeRows(None, Seq(trusteeRow("John Doe", 0), trusteeRow("Jane Doe", 1)), Nil))

      "show the lead trustee required inset, without the lead trustee paragraph or change link" in {
        assertRenderedById(doc, "inset-text--addATrustee")

        assertNotRenderedById(doc, "data-list--leadTrustee")
        assertNotRenderedById(doc, "p--leadTrusteeContact")
        assertNotRenderedById(doc, "link--changeLeadTrustee")
      }
    }
  }

  "TrusteeSummary content" must {

    val doc = render(
      TrusteeRows(Some(leadTrusteeRow), Seq(trusteeRow("John Doe", 0)), Seq(trusteeRow("James Smith", 1)))
    )

    "say all trustees have the same legal responsibilities" in
      assertContainsTextForId(
        doc,
        "p--allTrusteesSameResponsibilities",
        "All trustees have the same legal responsibilities."
      )

    "explain what the lead trustee is responsible for" in
      assertContainsTextForId(
        doc,
        "p--leadTrusteeContact",
        "This is the person or business HMRC will contact to discuss the trust or send official documents to. The lead trustee is responsible for keeping the trust’s details up to date."
      )

    "show the lack mental capacity heading" in
      assertContainsTextForId(
        doc,
        "heading--lackMentalCapacity",
        "You believe that these trustees lack mental capacity"
      )

    "explain what lacking mental capacity means" in
      assertContainsTextForId(
        doc,
        "p--lackMentalCapacity-1",
        "If a trustee lacks mental capacity it means that they may not have the capacity to understand or make decisions about the trust. A trustee who lacks mental capacity will not be able to become a lead trustee."
      )

    "explain how to tell HMRC mental capacity has changed" in
      assertContainsTextForId(
        doc,
        "p--lackMentalCapacity-2",
        "If you believe that the mental capacity of a trustee has changed you can inform HMRC by changing their details and declaring the trust."
      )
  }

}

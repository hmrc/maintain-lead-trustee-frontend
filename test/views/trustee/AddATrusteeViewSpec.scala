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

import forms.trustee.AddATrusteeFormProvider
import models._
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.addAnother.{AddRow, TrusteeRows}
import views.behaviours.ViewBehaviours
import views.html.trustee.AddATrusteeView

import java.time.LocalDate

class AddATrusteeViewSpec extends ViewBehaviours {

  private val form: Form[AddATrustee] = new AddATrusteeFormProvider()()

  private val view: AddATrusteeView = viewFor[AddATrusteeView](Some(emptyUserAnswers))

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

  private val heading = messages("addATrustee.count.heading", rows.size)

  private def applyView(form: Form[_]): HtmlFormat.Appendable =
    view(form, rows, heading)(fakeRequest, messages)

  "AddATrustee view" must {

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))

    "use the trustee count as the browser title and H1" in {
      val doc = asDocument(applyView(form))

      assertEqualsMessage(doc, "title", "addATrustee.count.heading", rows.size)
      assertPageTitleEqualsMessage(doc, "addATrustee.count.heading", rows.size)
    }

    "show the trustee summary, including trustees who lack mental capacity" in {
      val doc = asDocument(applyView(form))

      assertRenderedById(doc, "p--allTrusteesSameResponsibilities")
      assertRenderedById(doc, "p--leadTrusteeContact")
      assertRenderedById(doc, "data-list--otherTrustees")
      assertRenderedById(doc, "heading--lackMentalCapacity")
    }

    "ask whether the user wants to add a new trustee, and submit to AddATrusteeController" in {
      val doc = asDocument(applyView(form))

      doc.select("legend").text()                      mustBe messages("addATrustee.additional-content")
      doc.select("input[type=radio][name=value]").size mustBe AddATrustee.options.size
      doc.select("form").attr("action")                mustBe controllers.routes.AddATrusteeController.submitAnother().url
    }

    "show the error summary and prefix the title when the question has not been answered" in {
      val doc = asDocument(applyView(form.bind(Map("value" -> ""))))

      assertRenderedByCssSelector(doc, ".govuk-error-summary")
      doc.title() must startWith(messages("error.browser.title.prefix"))
    }
  }

  "AddATrustee view rendered for a trust where the lead is the only trustee" must {

    val lead: LeadTrustee = LeadTrusteeIndividual(
      bpMatchStatus = None,
      name = Name("Joe", None, "Bloggs"),
      dateOfBirth = LocalDate.parse("1996-02-03"),
      phoneNumber = "tel",
      email = None,
      identification = NationalInsuranceNumber("nino"),
      address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
      countryOfResidence = None,
      nationality = None
    )

    val leadOnlyHeading = AllTrustees(Some(lead), Nil).addToHeading
    val doc             =
      asDocument(view(form, TrusteeRows(Some(leadTrusteeRow), Nil, Nil), leadOnlyHeading)(fakeRequest, messages))

    "show 'The trust has 1 trustee' as the H1" in {
      doc.getElementsByTag("h1").first.text mustBe "The trust has 1 trustee"
    }

    "show 'The trust has 1 trustee' in the page title" in {
      doc.title mustBe "The trust has 1 trustee - Trustees - Manage a trust - GOV.UK"
    }
  }

}

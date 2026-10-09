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

package views

import forms.ReplaceLeadTrusteeFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.RadioOption
import views.behaviours.StringViewBehaviours
import views.html.ReplacingLeadTrusteeView

class ReplacingLeadTrusteeViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "replacingLeadTrustee"

  val form = new ReplaceLeadTrusteeFormProvider().withPrefix(messageKeyPrefix)

  val view = viewFor[ReplacingLeadTrusteeView](Some(emptyUserAnswers))

  val name = "Lead Trustee"

  val radioOptions = List(RadioOption(s"$messageKeyPrefix.-1", "-1", s"$messageKeyPrefix.add-new"))

  def applyView(form: Form[_], hasTrusteesLackingMentalCapacity: Boolean = false): HtmlFormat.Appendable =
    view.apply(form, name, radioOptions, hasTrusteesLackingMentalCapacity)(fakeRequest, messages)

  "ReplacingLeadTrustee view" must {

    behave like normalPage(applyView(form), messageKeyPrefix, "p1")

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))

    "have a single H1, with the radio question as the legend" in {
      val doc = asDocument(applyView(form))

      doc.getElementsByTag("h1").size mustBe 1
      doc.select("legend").size       mustBe 1
    }

    "still tell the user the current lead trustee will stay on the trust" in {
      val doc = asDocument(applyView(form))

      assertContainsHint(doc, "value", Some(messages(s"$messageKeyPrefix.hint", name)))
    }
  }

  "ReplacingLeadTrustee view" when {

    "there are trustees who lack mental capacity" must {
      val doc = asDocument(applyView(form, hasTrusteesLackingMentalCapacity = true))

      "explain why some trustees are not offered, under an H2" in {
        doc.getElementById("heading--mentalCapacity").tagName() mustBe "h2"
        assertRenderedById(doc, "p--mentalCapacity-1")
      }

      "link back to the trustees summary page so the user can check which trustees lack mental capacity" in {
        doc.getElementById("link--checkMentalCapacity").attr("href") mustBe
          controllers.routes.AddATrusteeController.onPageLoad().url
      }

      "show the mental capacity content before the radio question" in {
        val html = doc.toString

        html.indexOf("heading--mentalCapacity") must be < html.indexOf("<legend")
      }
    }

    "no trustees lack mental capacity" must {
      val doc = asDocument(applyView(form, hasTrusteesLackingMentalCapacity = false))

      "not show the mental capacity content, as every existing trustee can be offered" in {
        assertNotRenderedById(doc, "heading--mentalCapacity")
        assertNotRenderedById(doc, "p--mentalCapacity-1")
        assertNotRenderedById(doc, "p--mentalCapacity-2")
        assertNotRenderedById(doc, "link--checkMentalCapacity")
      }
    }
  }

  "ReplacingLeadTrustee content" must {

    val doc = asDocument(applyView(form, hasTrusteesLackingMentalCapacity = true))

    "show 'Choosing a new lead trustee' as the H1" in {
      doc.getElementsByTag("h1").first.text mustBe "Choosing a new lead trustee"
    }

    "show the page title" in {
      doc.title mustBe "Choosing a new lead trustee - Trustees - Manage a trust - GOV.UK"
    }

    "show the intro paragraph" in
      assertContainsTextForId(
        doc,
        "p--replacingLeadTrustee",
        "You can choose to make an existing trustee the new lead trustee if there are suitable trustees added to the trust."
      )

    "show the mental capacity heading" in
      assertContainsTextForId(doc, "heading--mentalCapacity", "If a trustee does not have mental capacity")

    "explain that trustees without mental capacity cannot become lead trustee" in
      assertContainsTextForId(
        doc,
        "p--mentalCapacity-1",
        "Trustees who do not have mental capacity to understand information about the trust will not be able to become lead trustee."
      )

    "link to check which trustees lack mental capacity" in
      assertContainsTextForId(doc, "p--mentalCapacity-2", "You can check which trustees lack mental capacity.")

    "ask 'Who will be the new lead trustee?' as the legend" in {
      doc.select("legend").text mustBe "Who will be the new lead trustee?"
    }
  }

  for (option <- radioOptions)
    s"rendered with a value of '${option.value}'" must {
      s"have the '${option.value}' radio button selected" in {
        val doc = asDocument(applyView(form.bind(Map("value" -> s"${option.value}"))))

        assertContainsRadioButton(doc, option.id, "value", option.value, isChecked = true)

        for (unselectedOption <- radioOptions.filterNot(o => o == option))
          assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, isChecked = false)
      }
    }

}

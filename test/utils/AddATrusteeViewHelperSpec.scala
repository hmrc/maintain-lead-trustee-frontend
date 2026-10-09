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

package utils

import base.SpecBase
import models.{
  AllTrustees, LeadTrusteeIndividual, Name, NationalInsuranceNumber, TrusteeIndividual, TrusteeOrganisation, UkAddress,
  YesNoDontKnow
}
import viewmodels.addAnother.{AddRow, TrusteeRows}

import java.time.LocalDate

class AddATrusteeViewHelperSpec extends SpecBase {

  private val leadTrustee = LeadTrusteeIndividual(
    bpMatchStatus = None,
    name = Name("Lead", None, "Trustee"),
    dateOfBirth = LocalDate.parse("1980-01-01"),
    phoneNumber = "+446565657",
    email = None,
    identification = NationalInsuranceNumber("JP121212A"),
    address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  )

  private val leadTrusteeRow = AddRow(
    name = "Lead Trustee",
    typeLabel = "Lead Trustee Individual",
    changeLabel = "Change details",
    changeUrl = "/maintain-a-trust/trustees/lead-trustee/individual/check-details",
    removeLabel = Some("Cannot remove"),
    removeUrl = None
  )

  private def individual(firstName: String, answer: Option[YesNoDontKnow]): TrusteeIndividual = TrusteeIndividual(
    name = Name(firstName = firstName, middleName = None, lastName = "Last"),
    dateOfBirth = None,
    phoneNumber = None,
    identification = None,
    address = None,
    mentalCapacityYesNo = answer,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  private def individualRow(firstName: String, index: Int): AddRow = AddRow(
    name = s"$firstName Last",
    typeLabel = "Trustee Individual",
    changeLabel = "Change details",
    changeUrl = s"/maintain-a-trust/trustees/trustee/individual/$index/check-details",
    removeLabel = Some("Remove"),
    removeUrl = Some(s"/maintain-a-trust/trustees/trustee/$index/remove")
  )

  private val organisation = TrusteeOrganisation(
    name = "Trustee Org",
    phoneNumber = None,
    email = None,
    identification = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  private def organisationRow(index: Int): AddRow = AddRow(
    name = "Trustee Org",
    typeLabel = "Trustee Company",
    changeLabel = "Change details",
    changeUrl = s"/maintain-a-trust/trustees/trustee/organisation/$index/check-details",
    removeLabel = Some("Remove"),
    removeUrl = Some(s"/maintain-a-trust/trustees/trustee/$index/remove")
  )

  "AddATrusteeViewHelper" when {

    ".groupedRows" must {

      "return no rows when there are no trustees" in {
        val result = new AddATrusteeViewHelper(AllTrustees(None, Nil)).groupedRows

        result      mustBe TrusteeRows(None, Nil, Nil)
        result.size mustBe 0
      }

      "return only the lead trustee when there are no other trustees" in {
        val result = new AddATrusteeViewHelper(AllTrustees(Some(leadTrustee), Nil)).groupedRows

        result      mustBe TrusteeRows(Some(leadTrusteeRow), Nil, Nil)
        result.size mustBe 1
      }

      "put individuals who answered 'Yes', 'I don't know' or did not answer, and organisations, in other trustees, as they are treated as having mental capacity" in {
        val trustees = List(
          individual("Yes", Some(YesNoDontKnow.Yes)),
          individual("DontKnow", Some(YesNoDontKnow.DontKnow)),
          individual("Unanswered", None),
          organisation
        )

        val result = new AddATrusteeViewHelper(AllTrustees(Some(leadTrustee), trustees)).groupedRows

        result.lead mustBe Some(leadTrusteeRow)

        result.otherTrustees mustBe List(
          individualRow("Yes", 0),
          individualRow("DontKnow", 1),
          individualRow("Unanswered", 2),
          organisationRow(3)
        )

        result.lackingMentalCapacity mustBe Nil
        result.size                  mustBe 5
      }

      "put only individuals who answered 'No' in lacking mental capacity, keeping each trustee's original index in their change and remove links" in {
        val trustees = List(
          individual("Capable", Some(YesNoDontKnow.Yes)),
          individual("LacksOne", Some(YesNoDontKnow.No)),
          organisation,
          individual("LacksTwo", Some(YesNoDontKnow.No))
        )

        val result = new AddATrusteeViewHelper(AllTrustees(Some(leadTrustee), trustees)).groupedRows

        result.lead                  mustBe Some(leadTrusteeRow)
        result.otherTrustees         mustBe List(individualRow("Capable", 0), organisationRow(2))
        result.lackingMentalCapacity mustBe List(individualRow("LacksOne", 1), individualRow("LacksTwo", 3))
        result.size                  mustBe 5
      }
    }
  }

}

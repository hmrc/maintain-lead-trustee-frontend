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

package models

import base.SpecBase

import java.time.LocalDate

class AllTrusteesSpec extends SpecBase {

  private val lead: LeadTrustee = LeadTrusteeIndividual(
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

  private val trustee: Trustee = TrusteeIndividual(
    name = Name("First", None, "Last"),
    dateOfBirth = None,
    phoneNumber = None,
    identification = None,
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = false
  )

  "AllTrustees.addToHeading" must {

    "be 'Add a trustee' when the trust has no trustees" in {
      AllTrustees(None, Nil).addToHeading mustBe "Add a trustee"
    }

    "be 'The trust has 1 trustee' when the lead is the only trustee" in {
      AllTrustees(Some(lead), Nil).addToHeading mustBe "The trust has 1 trustee"
    }

    "be 'The trust has 1 trustee' when there is one trustee and no lead" in {
      AllTrustees(None, List(trustee)).addToHeading mustBe "The trust has 1 trustee"
    }

    "use the plural from two trustees upwards" in {
      AllTrustees(Some(lead), List(trustee)).addToHeading mustBe "The trust has 2 trustees"
    }
  }

}

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

package utils

import base.SpecBase
import models.{AllTrustees, Name, NationalInsuranceNumber, TrustIdentificationOrgType, TrusteeIndividual, TrusteeOrganisation}
import viewmodels.addAnother.AddRow

import java.time.LocalDate

class AddATrusteeViewHelperSpec extends SpecBase {

  val trustees = List(
    TrusteeIndividual(
      name = Name(firstName = "First", middleName = None, lastName = "Last"),
      dateOfBirth = Some(LocalDate.parse("1983-09-24")),
      phoneNumber = None,
      identification = Some(NationalInsuranceNumber("JS123456A")),
      address = None,
      entityStart = LocalDate.parse("2019-02-28"),
      provisional = true
    ),
    TrusteeOrganisation(
      name = "Trustee Org",
      phoneNumber = None,
      email = None,
      identification = Some(TrustIdentificationOrgType(None, Some("1234567890"), None)),
      entityStart = LocalDate.parse("2019-02-28"),
      provisional = true
    )
  )

  "AddATrusteeViewHelper" when {

    ".row" must {

      "generate Nil for no user answers" in {
        val rows = new AddATrusteeViewHelper(AllTrustees(None, Nil)).rows
        rows.inProgress mustBe Nil
        rows.complete mustBe Nil
      }

      "generate rows from user answers for trustees" in {
        val rows = new AddATrusteeViewHelper(AllTrustees(None, trustees)).rows
        rows.complete mustBe List(
          AddRow(name = "First Last", typeLabel = "Trustee Individual", changeLabel = "Change details", changeUrl = "/maintain-a-trust/trustees/trustee/individual/0/check-details", removeLabel = Some("Remove"), removeUrl = Some("/maintain-a-trust/trustees/trustee/0/remove")),
          AddRow(name = "Trustee Org", typeLabel = "Trustee Company", changeLabel = "Change details" , changeUrl = "/maintain-a-trust/trustees/trustee/organisation/1/check-details", removeLabel = Some("Remove"), removeUrl = Some("/maintain-a-trust/trustees/trustee/1/remove"))
        )
        rows.inProgress mustBe Nil
      }

    }
  }
}

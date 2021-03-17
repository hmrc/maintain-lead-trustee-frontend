/*
 * Copyright 2021 HM Revenue & Customs
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

package mapping.mappers.trustee

import base.SpecBase
import models.Constants.GB
import models._
import pages.trustee.WhenAddedPage
import pages.trustee.organisation._

import java.time.LocalDate

class TrusteeOrganisationMapperSpec extends SpecBase {

  private val name: String = "Name"
  private val startDate: LocalDate = LocalDate.parse("2021-01-01")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "country")
  private val utr: String = "utr"

  private val mapper: TrusteeOrganisationMapper = new TrusteeOrganisationMapper()

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  "Trustee organisation mapper" must {

    "map user answers to trustee organisation" when {

      "trustee has UTR" in {
        val userAnswers = baseAnswers
          .set(UtrPage, utr).success.value
          .set(WhenAddedPage, startDate).success.value

        val result = mapper.map(userAnswers).get

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, Some(utr), None)),
          entityStart = startDate,
          provisional = true
        )
      }

      "trustee has UK address" in {
        val userAnswers = baseAnswers
          .set(AddressYesNoPage, true).success.value
          .set(AddressInTheUkYesNoPage, true).success.value
          .set(UkAddressPage, ukAddress).success.value
          .set(WhenAddedPage, startDate).success.value

        val result = mapper.map(userAnswers).get

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, None, Some(ukAddress))),
          entityStart = startDate,
          provisional = true
        )
      }

      "trustee has non-UK address" in {
        val userAnswers = baseAnswers
          .set(AddressYesNoPage, true).success.value
          .set(AddressInTheUkYesNoPage, false).success.value
          .set(NonUkAddressPage, nonUkAddress).success.value
          .set(WhenAddedPage, startDate).success.value

        val result = mapper.map(userAnswers).get

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, None, Some(nonUkAddress))),
          entityStart = startDate,
          provisional = true
        )
      }

      "trustee has no UTR or address" in {
        val userAnswers = baseAnswers
          .set(AddressYesNoPage, false).success.value
          .set(WhenAddedPage, startDate).success.value

        val result = mapper.map(userAnswers).get

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = None,
          entityStart = startDate,
          provisional = true
        )
      }

      "trustee has GB Residency" in {
        val userAnswers = baseAnswers
          .set(AddressYesNoPage, false).success.value
          .set(WhenAddedPage, startDate).success.value
          .set(CountryOfResidenceYesNoPage, true).success.value
          .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

        val result = mapper.map(userAnswers).get

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = None,
          countryOfResidence = Some(GB),
          entityStart = startDate,
          provisional = true
        )

      }

      "trustee has US Residency" in {
        val userAnswers = baseAnswers
          .set(AddressYesNoPage, false).success.value
          .set(WhenAddedPage, startDate).success.value
          .set(CountryOfResidenceYesNoPage, true).success.value
          .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
          .set(CountryOfResidencePage, "US").success.value

        val result = mapper.map(userAnswers).get

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = None,
          countryOfResidence = Some("US"),
          entityStart = startDate,
          provisional = true
        )

      }

    }
  }
}

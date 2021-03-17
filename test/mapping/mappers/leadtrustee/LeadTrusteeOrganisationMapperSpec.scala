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

package mapping.mappers.leadtrustee

import base.SpecBase
import models.Constants.GB
import models._
import pages.leadtrustee.organisation._

class LeadTrusteeOrganisationMapperSpec extends SpecBase {

  private val name: String = "Name"
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, country)
  private val utr: String = "utr"
  private val phone: String = "tel"
  private val email: String = "email"

  private val mapper: LeadTrusteeOrganisationMapper = new LeadTrusteeOrganisationMapper()

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  "Lead trustee organisation mapper" must {

    "map user answers to lead trustee organisation" when {

      "4mld" when {

        "trustee has UTR, UK address and email" in {
          val userAnswers = baseAnswers
            .set(RegisteredInUkYesNoPage, true).success.value
            .set(UtrPage, utr).success.value
            .set(AddressInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(EmailAddressYesNoPage, true).success.value
            .set(EmailAddressPage, email).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = phone,
            email = Some(email),
            utr = Some(utr),
            address = ukAddress
          )
        }

        "trustee has no UTR, non-UK address and no email" in {
          val userAnswers = baseAnswers
            .set(RegisteredInUkYesNoPage, false).success.value
            .set(AddressInTheUkYesNoPage, false).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(EmailAddressYesNoPage, false).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = phone,
            email = None,
            utr = None,
            address = nonUkAddress
          )
        }
      }

      "5mld" when {

        "trustee has UTR, UK residency/address and email" in {
          val userAnswers = baseAnswers
            .set(RegisteredInUkYesNoPage, true).success.value
            .set(UtrPage, utr).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(EmailAddressYesNoPage, true).success.value
            .set(EmailAddressPage, email).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = phone,
            email = Some(email),
            utr = Some(utr),
            address = ukAddress,
            countryOfResidence = Some(GB)
          )
        }

        "trustee has no UTR, non-UK residency/address and no email" in {
          val userAnswers = baseAnswers
            .set(RegisteredInUkYesNoPage, false).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
            .set(CountryOfResidencePage, country).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(EmailAddressYesNoPage, false).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = phone,
            email = None,
            utr = None,
            address = nonUkAddress,
            countryOfResidence = Some(country)
          )
        }
      }
    }
  }
}

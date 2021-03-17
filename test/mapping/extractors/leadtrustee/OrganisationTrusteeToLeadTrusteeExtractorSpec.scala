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

package mapping.extractors.leadtrustee

import base.SpecBase
import models.Constants.GB
import models.IndividualOrBusiness.Business
import models.{NonUkAddress, TrustIdentificationOrgType, TrusteeOrganisation, UkAddress, UserAnswers}
import pages.leadtrustee.IndividualOrBusinessPage
import pages.leadtrustee.organisation._

import java.time.LocalDate

class OrganisationTrusteeToLeadTrusteeExtractorSpec extends SpecBase {

  private val index = 0

  private val name: String = "Name"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, country)
  private val utr: String = "utr"
  private val email: String = "email"
  private val tel: String = "phone"

  private val extractor: OrganisationTrusteeToLeadTrusteeExtractor = new OrganisationTrusteeToLeadTrusteeExtractor()

  "OrganisationTrusteeToLeadTrusteeExtractor" must {

    "populate user answers" when {

      "4mld" when {

        val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = false)

        "has a UTR" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = Some(tel),
            email = Some(email),
            identification = Some(TrustIdentificationOrgType(None, Some(utr), None)),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage).get mustBe true
          result.get(NamePage).get mustBe name
          result.get(UtrPage).get mustBe utr
          result.get(AddressInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage).get mustBe true
          result.get(EmailAddressPage).get mustBe email
          result.get(TelephoneNumberPage).get mustBe tel

        }

        "has a UK address" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = Some(tel),
            email = Some(email),
            identification = Some(TrustIdentificationOrgType(None, None, Some(ukAddress))),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage).get mustBe false
          result.get(NamePage).get mustBe name
          result.get(UtrPage) mustBe None
          result.get(AddressInTheUkYesNoPage).get mustBe true
          result.get(UkAddressPage).get mustBe ukAddress
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage).get mustBe true
          result.get(EmailAddressPage).get mustBe email
          result.get(TelephoneNumberPage).get mustBe tel

        }

        "has a non-UK address" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = Some(tel),
            email = Some(email),
            identification = Some(TrustIdentificationOrgType(None, None, Some(nonUkAddress))),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage).get mustBe false
          result.get(NamePage).get mustBe name
          result.get(UtrPage) mustBe None
          result.get(AddressInTheUkYesNoPage).get mustBe false
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage).get mustBe nonUkAddress
          result.get(EmailAddressYesNoPage).get mustBe true
          result.get(EmailAddressPage).get mustBe email
          result.get(TelephoneNumberPage).get mustBe tel

        }

        "has no UTR or address" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = None,
            email = None,
            identification = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage) mustBe None
          result.get(NamePage).get mustBe name
          result.get(UtrPage) mustBe None
          result.get(AddressInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None

        }
      }

      "5mld" when {

        val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true)

        "trustee has minimum data" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = None,
            email = None,
            identification = None,
            countryOfResidence = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage) mustBe None
          result.get(NamePage).get mustBe name
          result.get(UtrPage) mustBe None
          result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
          result.get(CountryOfResidencePage) mustBe None
          result.get(AddressInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None
        }

        "trustee has UK residency" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = None,
            email = None,
            identification = None,
            countryOfResidence = Some(GB),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage) mustBe None
          result.get(NamePage).get mustBe name
          result.get(UtrPage) mustBe None
          result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe true
          result.get(CountryOfResidencePage).get mustBe GB
          result.get(AddressInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None
        }

        "trustee has non-UK residency" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = None,
            email = None,
            identification = None,
            countryOfResidence = Some(country),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(RegisteredInUkYesNoPage) mustBe None
          result.get(NamePage).get mustBe name
          result.get(UtrPage) mustBe None
          result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
          result.get(CountryOfResidencePage).get mustBe country
          result.get(AddressInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None
        }
      }
    }
  }
}

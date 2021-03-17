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

package mapping.extractors.trustee

import base.SpecBase
import models.Constants.GB
import models.IndividualOrBusiness.Business
import models.{NonUkAddress, TrustIdentificationOrgType, TrusteeOrganisation, UkAddress, UserAnswers}
import pages.trustee.IndividualOrBusinessPage
import pages.trustee.organisation._
import pages.trustee.organisation.amend._

import java.time.LocalDate

class TrusteeOrganisationExtractorSpec extends SpecBase {

  private val index = 0

  private val name: String = "Name"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "country")

  private val extractor = new TrusteeOrganisationExtractor()

  "TrusteeOrganisationExtractor" must {

    "4mld" when {

      "should populate user answers when trustee has a UTR" in {

        val trustee = TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, Some("utr"), None)),
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Business
        result.get(NamePage).get mustBe name
        result.get(UtrYesNoPage).get mustBe true
        result.get(UtrPage).get mustBe "utr"
        result.get(AddressYesNoPage)  mustBe None
        result.get(AddressInTheUkYesNoPage)  mustBe None
        result.get(UkAddressPage)  mustBe None
        result.get(NonUkAddressPage)  mustBe None

      }

      "should populate user answers when trustee has a UK address" in {

        val trustee = TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, None, Some(ukAddress))),
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Business
        result.get(NamePage).get mustBe name
        result.get(UtrYesNoPage).get mustBe false
        result.get(UtrPage)  mustBe None
        result.get(AddressYesNoPage).get mustBe true
        result.get(AddressInTheUkYesNoPage).get mustBe true
        result.get(UkAddressPage).get mustBe ukAddress
        result.get(NonUkAddressPage)  mustBe None

      }

      "should populate user answers when trustee has a non-UK address" in {

        val trustee = TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, None, Some(nonUkAddress))),
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Business
        result.get(NamePage).get mustBe name
        result.get(UtrYesNoPage).get mustBe false
        result.get(UtrPage)  mustBe None
        result.get(AddressYesNoPage).get mustBe true
        result.get(AddressInTheUkYesNoPage).get mustBe false
        result.get(UkAddressPage)  mustBe None
        result.get(NonUkAddressPage).get mustBe nonUkAddress

      }

      "should populate user answers when trustee has no UTR or address" in {

        val trustee = TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = Some(TrustIdentificationOrgType(None, None, None)),
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Business
        result.get(NamePage).get mustBe name
        result.get(UtrYesNoPage).get mustBe false
        result.get(UtrPage)  mustBe None
        result.get(AddressYesNoPage).get mustBe false
        result.get(AddressInTheUkYesNoPage)  mustBe None
        result.get(UkAddressPage)  mustBe None
        result.get(NonUkAddressPage)  mustBe None

      }

    }

    "5mld" when {

      "taxable" when {

        "underlying trust data is 4mld" when {

          val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

          "has no country of residence" in {

            val trustee = TrusteeOrganisation(
              name = name,
              phoneNumber = None,
              email = None,
              identification = Some(TrustIdentificationOrgType(None, None, None)),
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndividualOrBusinessPage).get mustBe Business
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage).get mustBe false
            result.get(UtrPage)  mustBe None
            result.get(AddressYesNoPage).get mustBe false
            result.get(AddressInTheUkYesNoPage)  mustBe None
            result.get(UkAddressPage)  mustBe None
            result.get(NonUkAddressPage)  mustBe None
            result.get(CountryOfResidenceYesNoPage) mustBe None
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None

          }

          "has no country of residence but does have an address" in {

            val trustee = TrusteeOrganisation(
              name = name,
              phoneNumber = None,
              email = None,
              identification = Some(TrustIdentificationOrgType(None, None, Some(ukAddress))),
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndividualOrBusinessPage).get mustBe Business
            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage).get mustBe false
            result.get(UtrPage)  mustBe None
            result.get(AddressYesNoPage).get mustBe true
            result.get(AddressInTheUkYesNoPage).get mustBe true
            result.get(UkAddressPage).get mustBe ukAddress
            result.get(NonUkAddressPage)  mustBe None
            result.get(CountryOfResidenceYesNoPage) mustBe None
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None

          }

        }

        "underlying trust data is 5mld" when {
          val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = true)

          "has no country of residence" in {

            val trustee = TrusteeOrganisation(
              name = name,
              phoneNumber = None,
              email = None,
              identification = Some(TrustIdentificationOrgType(None, None, None)),
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(IndividualOrBusinessPage).get mustBe Business
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage).get mustBe false
            result.get(UtrPage)  mustBe None
            result.get(AddressYesNoPage).get mustBe false
            result.get(AddressInTheUkYesNoPage)  mustBe None
            result.get(UkAddressPage)  mustBe None
            result.get(NonUkAddressPage)  mustBe None
            result.get(CountryOfResidenceYesNoPage).get mustBe false
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None

          }

          "has no country of residence but does have an address" in {

            val trustee = TrusteeOrganisation(
              name = name,
              phoneNumber = None,
              email = None,
              identification = Some(TrustIdentificationOrgType(None, None, Some(ukAddress))),
              countryOfResidence = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(IndividualOrBusinessPage).get mustBe Business
            result.get(NamePage).get mustBe name
            result.get(UtrYesNoPage).get mustBe false
            result.get(UtrPage)  mustBe None
            result.get(AddressYesNoPage).get mustBe true
            result.get(AddressInTheUkYesNoPage).get mustBe true
            result.get(UkAddressPage).get mustBe ukAddress
            result.get(NonUkAddressPage)  mustBe None
            result.get(CountryOfResidenceYesNoPage).get mustBe false
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None

          }

          "has a country of residence in GB" in {

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

            result.get(IndexPage).get mustBe index
            result.get(IndividualOrBusinessPage).get mustBe Business
            result.get(NamePage).get mustBe name
            result.get(CountryOfResidenceYesNoPage).get mustBe true
            result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe true
            result.get(CountryOfResidencePage).get mustBe GB

          }


          "has a country of residence in Spain" in {

            val trustee = TrusteeOrganisation(
              name = name,
              phoneNumber = None,
              email = None,
              identification = None,
              countryOfResidence = Some("Spain"),
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(IndividualOrBusinessPage).get mustBe Business
            result.get(NamePage).get mustBe name
            result.get(CountryOfResidenceYesNoPage).get mustBe true
            result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
            result.get(CountryOfResidencePage).get mustBe "Spain"

          }

        }

      }

      "non-taxable" when {

        val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false, isUnderlyingData5mld = true)

        "has no country of residence" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = None,
            email = None,
            identification = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(NamePage).get mustBe name
          result.get(UtrYesNoPage)  mustBe None
          result.get(UtrPage)  mustBe None
          result.get(AddressYesNoPage)  mustBe None
          result.get(AddressInTheUkYesNoPage)  mustBe None
          result.get(UkAddressPage)  mustBe None
          result.get(NonUkAddressPage)  mustBe None
          result.get(CountryOfResidenceYesNoPage).get mustBe false
          result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
          result.get(CountryOfResidencePage) mustBe None

        }

        "has a country of residence in Spain" in {

          val trustee = TrusteeOrganisation(
            name = name,
            phoneNumber = None,
            email = None,
            identification = None,
            countryOfResidence = Some("Spain"),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(IndividualOrBusinessPage).get mustBe Business
          result.get(NamePage).get mustBe name
          result.get(UtrYesNoPage)  mustBe None
          result.get(UtrPage)  mustBe None
          result.get(AddressYesNoPage)  mustBe None
          result.get(AddressInTheUkYesNoPage)  mustBe None
          result.get(UkAddressPage)  mustBe None
          result.get(NonUkAddressPage)  mustBe None
          result.get(CountryOfResidenceYesNoPage).get mustBe true
          result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
          result.get(CountryOfResidencePage).get mustBe "Spain"

        }

      }

    }

  }

}

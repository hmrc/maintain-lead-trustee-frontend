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
import models.IndividualOrBusiness.Individual
import models._
import pages.trustee.IndividualOrBusinessPage
import pages.trustee.individual._
import pages.trustee.individual.amend._

import java.time.LocalDate

class TrusteeIndividualExtractorSpec extends SpecBase {

  private val index: Int = 0

  private val name: Name = Name("First", None, "Last")
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "country")
  private val nino: String = "nino"

  private val extractor: TrusteeIndividualExtractor = new TrusteeIndividualExtractor()

  "TrusteeIndividualExtractor" must {
    "4mld" when {
      "should populate user answers when trustee has a NINO" in {

        val identification = NationalInsuranceNumber(nino)

        val trustee = TrusteeIndividual(
          name = name,
          dateOfBirth = Some(date),
          phoneNumber = None,
          identification = Some(identification),
          address = None,
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Individual
        result.get(NamePage).get mustBe name
        result.get(DateOfBirthYesNoPage).get mustBe true
        result.get(DateOfBirthPage).get mustBe date
        result.get(NationalInsuranceNumberYesNoPage).get mustBe true
        result.get(NationalInsuranceNumberPage).get mustBe nino
        result.get(AddressYesNoPage) mustBe None
        result.get(LiveInTheUkYesNoPage) mustBe None
        result.get(UkAddressPage) mustBe None
        result.get(NonUkAddressPage) mustBe None
        result.get(PassportDetailsYesNoPage) mustBe None
        result.get(PassportDetailsPage) mustBe None
        result.get(IdCardDetailsYesNoPage) mustBe None
        result.get(IdCardDetailsPage) mustBe None
        result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
        result.get(PassportOrIdCardDetailsPage) mustBe None
        result.get(ProvisionalIdDetailsPage).get mustBe true

      }

      "should populate user answers when trustee has a UK address and passport/ID card" in {

        val combined = CombinedPassportOrIdCard("country", "number", date)

        val trustee = TrusteeIndividual(
          name = name,
          dateOfBirth = Some(date),
          phoneNumber = None,
          identification = Some(combined),
          address = Some(ukAddress),
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Individual
        result.get(NamePage).get mustBe name
        result.get(DateOfBirthYesNoPage).get mustBe true
        result.get(DateOfBirthPage).get mustBe date
        result.get(NationalInsuranceNumberYesNoPage).get mustBe false
        result.get(NationalInsuranceNumberPage) mustBe None
        result.get(AddressYesNoPage).get mustBe true
        result.get(LiveInTheUkYesNoPage).get mustBe true
        result.get(UkAddressPage).get mustBe ukAddress
        result.get(NonUkAddressPage) mustBe None
        result.get(PassportDetailsYesNoPage) mustBe None
        result.get(PassportDetailsPage) mustBe None
        result.get(IdCardDetailsYesNoPage) mustBe None
        result.get(IdCardDetailsPage) mustBe None
        result.get(PassportOrIdCardDetailsYesNoPage).get mustBe true
        result.get(PassportOrIdCardDetailsPage).get mustBe combined
        result.get(ProvisionalIdDetailsPage).get mustBe false

      }

      "should populate user answers when trustee has a non-UK address" in {

        val trustee = TrusteeIndividual(
          name = name,
          dateOfBirth = Some(date),
          phoneNumber = None,
          identification = None,
          address = Some(nonUkAddress),
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Individual
        result.get(NamePage).get mustBe name
        result.get(DateOfBirthYesNoPage).get mustBe true
        result.get(DateOfBirthPage).get mustBe date
        result.get(NationalInsuranceNumberYesNoPage).get mustBe false
        result.get(NationalInsuranceNumberPage) mustBe None
        result.get(AddressYesNoPage).get mustBe true
        result.get(LiveInTheUkYesNoPage).get mustBe false
        result.get(UkAddressPage) mustBe None
        result.get(NonUkAddressPage).get mustBe nonUkAddress
        result.get(PassportDetailsYesNoPage) mustBe None
        result.get(PassportDetailsPage) mustBe None
        result.get(IdCardDetailsYesNoPage) mustBe None
        result.get(IdCardDetailsPage) mustBe None
        result.get(PassportOrIdCardDetailsYesNoPage).get mustBe false
        result.get(PassportOrIdCardDetailsPage) mustBe None
        result.get(ProvisionalIdDetailsPage).get mustBe true

      }

      "should populate user answers when trustee has minimum data" in {

        val trustee = TrusteeIndividual(
          name = name,
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          entityStart = date,
          provisional = true
        )

        val result = extractor.extract(emptyUserAnswers, trustee, index).get

        result.get(IndividualOrBusinessPage).get mustBe Individual
        result.get(NamePage).get mustBe name
        result.get(DateOfBirthYesNoPage).get mustBe false
        result.get(DateOfBirthPage) mustBe None
        result.get(NationalInsuranceNumberYesNoPage).get mustBe false
        result.get(NationalInsuranceNumberPage) mustBe None
        result.get(AddressYesNoPage).get mustBe false
        result.get(LiveInTheUkYesNoPage) mustBe None
        result.get(UkAddressPage) mustBe None
        result.get(NonUkAddressPage) mustBe None
        result.get(PassportDetailsYesNoPage) mustBe None
        result.get(PassportDetailsPage) mustBe None
        result.get(IdCardDetailsYesNoPage) mustBe None
        result.get(IdCardDetailsPage) mustBe None
        result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
        result.get(PassportOrIdCardDetailsPage) mustBe None
        result.get(ProvisionalIdDetailsPage).get mustBe true

      }
    }

    "5mld" when {
      "taxable" when {
        "underlying trust data is 4mld" when {
          val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)
          "has no country of residence, nationality or mental capacity" in {

            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndividualOrBusinessPage).get mustBe Individual
            result.get(NamePage).get mustBe name
            result.get(DateOfBirthYesNoPage).get mustBe false
            result.get(DateOfBirthPage) mustBe None
            result.get(NationalInsuranceNumberYesNoPage).get mustBe false
            result.get(NationalInsuranceNumberPage) mustBe None
            result.get(CountryOfResidenceYesNoPage) mustBe None
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(CountryOfNationalityYesNoPage) mustBe None
            result.get(CountryOfNationalityInTheUkYesNoPage) mustBe None
            result.get(CountryOfNationalityPage) mustBe None
            result.get(AddressYesNoPage).get mustBe false
            result.get(LiveInTheUkYesNoPage) mustBe None
            result.get(UkAddressPage) mustBe None
            result.get(NonUkAddressPage) mustBe None
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
            result.get(PassportOrIdCardDetailsPage) mustBe None
            result.get(MentalCapacityYesNoPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has no country of residence but does have an address" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              countryOfResidence = None,
              address = Some(ukAddress),
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(CountryOfResidenceYesNoPage) mustBe None
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(AddressYesNoPage).get mustBe true
            result.get(LiveInTheUkYesNoPage).get mustBe true
            result.get(UkAddressPage).get mustBe ukAddress
            result.get(NonUkAddressPage) mustBe None
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }
        }

        "underlying trust data is 5mld" when {
          val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = true)

          "has no country of residence, nationality or mental capacity" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(IndividualOrBusinessPage).get mustBe Individual
            result.get(NamePage).get mustBe name
            result.get(DateOfBirthYesNoPage).get mustBe false
            result.get(DateOfBirthPage) mustBe None
            result.get(NationalInsuranceNumberYesNoPage).get mustBe false
            result.get(NationalInsuranceNumberPage) mustBe None
            result.get(CountryOfResidenceYesNoPage).get mustBe false
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(CountryOfNationalityYesNoPage).get mustBe false
            result.get(CountryOfNationalityInTheUkYesNoPage) mustBe None
            result.get(CountryOfNationalityPage) mustBe None
            result.get(AddressYesNoPage).get mustBe false
            result.get(LiveInTheUkYesNoPage) mustBe None
            result.get(UkAddressPage) mustBe None
            result.get(NonUkAddressPage) mustBe None
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(MentalCapacityYesNoPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has no country of residence but does have an address" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              countryOfResidence = None,
              address = Some(ukAddress),
              entityStart = date,
              provisional = true
            )


            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(CountryOfResidenceYesNoPage).get mustBe false
            result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(AddressYesNoPage).get mustBe true
            result.get(LiveInTheUkYesNoPage).get mustBe true
            result.get(UkAddressPage).get mustBe ukAddress
            result.get(NonUkAddressPage) mustBe None
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has a country of residence in GB" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              countryOfResidence = Some(GB),
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(CountryOfResidenceYesNoPage).get mustBe true
            result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe true
            result.get(CountryOfResidencePage).get mustBe GB
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has a country of residence in Spain" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              countryOfResidence = Some("Spain"),
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(CountryOfResidenceYesNoPage).get mustBe true
            result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
            result.get(CountryOfResidencePage).get mustBe "Spain"
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has a country of nationality in GB" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              nationality = Some(GB),
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(CountryOfNationalityYesNoPage).get mustBe true
            result.get(CountryOfNationalityInTheUkYesNoPage).get mustBe true
            result.get(CountryOfNationalityPage).get mustBe GB
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has a country of nationality in Spain" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              nationality = Some("Spain"),
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(CountryOfNationalityYesNoPage).get mustBe true
            result.get(CountryOfNationalityInTheUkYesNoPage).get mustBe false
            result.get(CountryOfNationalityPage).get mustBe "Spain"
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has a mental capacity" in {
            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = None,
              nationality = None,
              mentalCapacityYesNo = Some(true),
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(MentalCapacityYesNoPage).get mustBe true
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }
          
          "has an NINO" in {
            val identification = NationalInsuranceNumber(nino)

            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = Some(identification),
              countryOfResidence = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(NationalInsuranceNumberYesNoPage).get mustBe true
            result.get(NationalInsuranceNumberPage).get mustBe nino
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has passport details" in {
            val passport = Passport("country", "number", date)

            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = Some(passport),
              countryOfResidence = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(PassportDetailsYesNoPage).get mustBe true
            result.get(PassportDetailsPage).get mustBe passport
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
            result.get(PassportOrIdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has ID Card details" in {
            val idCard = IdCard("country", "number", date)

            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = Some(idCard),
              countryOfResidence = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(PassportDetailsYesNoPage).get mustBe false
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage).get mustBe true
            result.get(IdCardDetailsPage).get mustBe idCard
            result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
            result.get(PassportOrIdCardDetailsPage) mustBe None
            result.get(ProvisionalIdDetailsPage).get mustBe true
          }

          "has passport or ID card details" in {
            val combined = CombinedPassportOrIdCard("country", "number", date)

            val trustee = TrusteeIndividual(
              name = name,
              dateOfBirth = None,
              phoneNumber = None,
              identification = Some(combined),
              countryOfResidence = None,
              address = None,
              entityStart = date,
              provisional = true
            )

            val result = extractor.extract(baseAnswers, trustee, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(PassportDetailsYesNoPage) mustBe None
            result.get(PassportDetailsPage) mustBe None
            result.get(IdCardDetailsYesNoPage) mustBe None
            result.get(IdCardDetailsPage) mustBe None
            result.get(PassportOrIdCardDetailsYesNoPage).get mustBe true
            result.get(PassportOrIdCardDetailsPage).get mustBe combined
            result.get(ProvisionalIdDetailsPage).get mustBe false
          }

        }
      }

      "non taxable" when {
        val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false, isUnderlyingData5mld = true)

        "has no country of residence, nationality or mental capacity" in {
          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            address = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(CountryOfResidenceYesNoPage).get mustBe false
          result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
          result.get(CountryOfResidencePage) mustBe None
          result.get(CountryOfNationalityYesNoPage).get mustBe false
          result.get(CountryOfNationalityInTheUkYesNoPage) mustBe None
          result.get(CountryOfNationalityPage) mustBe None
          result.get(MentalCapacityYesNoPage) mustBe None
          result.get(PassportDetailsYesNoPage) mustBe None
          result.get(PassportDetailsPage) mustBe None
          result.get(IdCardDetailsYesNoPage) mustBe None
          result.get(IdCardDetailsPage) mustBe None
          result.get(ProvisionalIdDetailsPage).get mustBe true
        }

        "has a country of residence and nationality in Spain" in {
          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = Some("Spain"),
            nationality = Some("Spain"),
            address = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(CountryOfResidenceYesNoPage).get mustBe true
          result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
          result.get(CountryOfResidencePage).get mustBe "Spain"
          result.get(CountryOfNationalityYesNoPage).get mustBe true
          result.get(CountryOfNationalityInTheUkYesNoPage).get mustBe false
          result.get(CountryOfNationalityPage).get mustBe "Spain"
          result.get(PassportDetailsYesNoPage) mustBe None
          result.get(PassportDetailsPage) mustBe None
          result.get(IdCardDetailsYesNoPage) mustBe None
          result.get(IdCardDetailsPage) mustBe None
          result.get(ProvisionalIdDetailsPage).get mustBe true
        }

        "has an address" in {
          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = None,
            address = Some(nonUkAddress),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(AddressYesNoPage) mustBe None
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(PassportDetailsYesNoPage) mustBe None
          result.get(PassportDetailsPage) mustBe None
          result.get(IdCardDetailsYesNoPage) mustBe None
          result.get(IdCardDetailsPage) mustBe None
          result.get(ProvisionalIdDetailsPage).get mustBe true
        }

        "has a mental capacity" in {
          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            nationality = None,
            mentalCapacityYesNo = Some(true),
            address = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(MentalCapacityYesNoPage).get mustBe true
          result.get(PassportDetailsYesNoPage) mustBe None
          result.get(PassportDetailsPage) mustBe None
          result.get(IdCardDetailsYesNoPage) mustBe None
          result.get(IdCardDetailsPage) mustBe None
          result.get(ProvisionalIdDetailsPage).get mustBe true
        }

        "has an NINO" in {
          val identification = NationalInsuranceNumber(nino)

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = Some(identification),
            countryOfResidence = None,
            address = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(NationalInsuranceNumberYesNoPage) mustBe None
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportDetailsYesNoPage) mustBe None
          result.get(PassportDetailsPage) mustBe None
          result.get(IdCardDetailsYesNoPage) mustBe None
          result.get(IdCardDetailsPage) mustBe None
          result.get(ProvisionalIdDetailsPage).get mustBe true
        }

        "has a Passport or Card Details" in {
          val combined = CombinedPassportOrIdCard("country", "number", date)

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = Some(combined),
            countryOfResidence = None,
            address = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(PassportDetailsYesNoPage) mustBe None
          result.get(PassportDetailsPage) mustBe None
          result.get(IdCardDetailsYesNoPage) mustBe None
          result.get(IdCardDetailsPage) mustBe None
          result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(ProvisionalIdDetailsPage).get mustBe false
        }
      }
    }
  }
}

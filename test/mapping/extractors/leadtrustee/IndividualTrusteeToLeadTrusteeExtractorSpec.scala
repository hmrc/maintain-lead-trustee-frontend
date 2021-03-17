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
import models.IndividualOrBusiness.Individual
import models._
import pages.leadtrustee.IndividualOrBusinessPage
import pages.leadtrustee.individual._

import java.time.LocalDate

class IndividualTrusteeToLeadTrusteeExtractorSpec extends SpecBase {

  private val index: Int = 0

  private val name: Name = Name("First", None, "Last")
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, country)
  private val nino: String = "nino"
  private val tel: String = "phone"
  private val combined = CombinedPassportOrIdCard(country, "number", date)

  private val extractor: IndividualTrusteeToLeadTrusteeExtractor = new IndividualTrusteeToLeadTrusteeExtractor()

  "IndividualTrusteeToLeadTrusteeExtractor" must {

    "populate user answers" when {

      "4mld" when {

        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = false)

        "trustee has a NINO" in {

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = Some(date),
            phoneNumber = Some(tel),
            identification = Some(NationalInsuranceNumber(nino)),
            address = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Individual
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthPage).get mustBe date
          result.get(UkCitizenPage).get mustBe true
          result.get(NationalInsuranceNumberPage).get mustBe nino
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage).get mustBe tel

        }

        "trustee has a UK address and passport/ID card" in {

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = Some(date),
            phoneNumber = Some(tel),
            identification = Some(combined),
            address = Some(ukAddress),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Individual
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthPage).get mustBe date
          result.get(UkCitizenPage).get mustBe false
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportOrIdCardDetailsPage).get mustBe combined
          result.get(LiveInTheUkYesNoPage).get mustBe true
          result.get(UkAddressPage).get mustBe ukAddress
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage).get mustBe tel

        }

        "trustee has a non-UK address" in {

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = Some(date),
            phoneNumber = Some(tel),
            identification = None,
            address = Some(nonUkAddress),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Individual
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthPage).get mustBe date
          result.get(UkCitizenPage) mustBe None
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(LiveInTheUkYesNoPage).get mustBe false
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage).get mustBe nonUkAddress
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage).get mustBe tel

        }

        "trustee has minimum data" in {

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
          result.get(DateOfBirthPage) mustBe None
          result.get(UkCitizenPage) mustBe None
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None

        }
      }

      "5mld" when {

        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true)

        "trustee has minimum data" in {

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            address = None,
            countryOfResidence = None,
            nationality = None,
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Individual
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthPage) mustBe None
          result.get(CountryOfNationalityInTheUkYesNoPage) mustBe None
          result.get(CountryOfNationalityPage) mustBe None
          result.get(UkCitizenPage) mustBe None
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(CountryOfResidenceInTheUkYesNoPage) mustBe None
          result.get(CountryOfResidencePage) mustBe None
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None

        }

        "trustee has UK nationality and residency" in {

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            address = None,
            countryOfResidence = Some(GB),
            nationality = Some(GB),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Individual
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthPage) mustBe None
          result.get(CountryOfNationalityInTheUkYesNoPage).get mustBe true
          result.get(CountryOfNationalityPage).get mustBe GB
          result.get(UkCitizenPage) mustBe None
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe true
          result.get(CountryOfResidencePage).get mustBe GB
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(EmailAddressYesNoPage) mustBe None
          result.get(EmailAddressPage) mustBe None
          result.get(TelephoneNumberPage) mustBe None

        }

        "trustee has non-UK nationality and residency" in {

          val trustee = TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            address = None,
            countryOfResidence = Some(country),
            nationality = Some(country),
            entityStart = date,
            provisional = true
          )

          val result = extractor.extract(baseAnswers, trustee, index).get

          result.get(IndividualOrBusinessPage).get mustBe Individual
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthPage) mustBe None
          result.get(CountryOfNationalityInTheUkYesNoPage).get mustBe false
          result.get(CountryOfNationalityPage).get mustBe country
          result.get(UkCitizenPage) mustBe None
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(CountryOfResidenceInTheUkYesNoPage).get mustBe false
          result.get(CountryOfResidencePage).get mustBe country
          result.get(LiveInTheUkYesNoPage) mustBe None
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

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
import models.YesNoDontKnow.{No, Yes}
import models._
import pages.trustee.individual._
import pages.trustee.individual.add._

import java.time.LocalDate

class TrusteeIndividualMapperSpec extends SpecBase {

  private val name: Name = Name("First", None, "Last")
  private val dateOfBirth: LocalDate = LocalDate.parse("1996-02-03")
  private val startDate: LocalDate = LocalDate.parse("2021-01-01")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "country")
  private val nino: String = "nino"

  private val country = "FR"
  private val number = "1234567890"
  private val expirationDate = LocalDate.parse("2020-02-03")
  private val passport: Passport = Passport(country, number, expirationDate)
  private val idCard: IdCard = IdCard(country, number, expirationDate)
  private val combined: CombinedPassportOrIdCard = CombinedPassportOrIdCard(country, number, expirationDate)

  private val mapper: TrusteeIndividualMapper = new TrusteeIndividualMapper()

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  "Trustee individual mapper" must {

    "map user answers to trustee individual" when {

      "adding" when {

        "trustee has date of birth and a NINO" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, true).success.value
            .set(DateOfBirthPage, dateOfBirth).success.value
            .set(NationalInsuranceNumberYesNoPage, true).success.value
            .set(NationalInsuranceNumberPage, nino).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = Some(dateOfBirth),
            phoneNumber = None,
            identification = Some(NationalInsuranceNumber(nino)),
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has no identification" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(AddressYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has UK address and passport" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(AddressYesNoPage, true).success.value
            .set(LiveInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(PassportDetailsYesNoPage, true).success.value
            .set(PassportDetailsPage, passport).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = Some(passport.asCombined),
            address = Some(ukAddress),
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has non-UK address and ID card" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(AddressYesNoPage, true).success.value
            .set(LiveInTheUkYesNoPage, false).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, true).success.value
            .set(IdCardDetailsPage, idCard).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = Some(idCard.asCombined),
            address = Some(nonUkAddress),
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee only has address" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(AddressYesNoPage, true).success.value
            .set(LiveInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            address = Some(ukAddress),
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has GB Residency" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(CountryOfResidenceYesNoPage, true).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, true).success.value
            .set(AddressYesNoPage, false).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = Some(GB),
            nationality = None,
            mentalCapacityYesNo = None,
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has US Residency" in {

          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(CountryOfResidenceYesNoPage, true).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
            .set(CountryOfResidencePage, "US").success.value
            .set(AddressYesNoPage, false).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = Some("US"),
            nationality = None,
            mentalCapacityYesNo = None,
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has GB Nationality" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(CountryOfNationalityYesNoPage, true).success.value
            .set(CountryOfNationalityInTheUkYesNoPage, true).success.value
            .set(AddressYesNoPage, false).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = None,
            nationality = Some(GB),
            mentalCapacityYesNo = None,
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has US Nationality" in {

          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(CountryOfNationalityYesNoPage, true).success.value
            .set(CountryOfNationalityInTheUkYesNoPage, false).success.value
            .set(CountryOfNationalityPage, "US").success.value
            .set(AddressYesNoPage, false).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = None,
            nationality = Some("US"),
            mentalCapacityYesNo = None,
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has Mental Capacity false" in {

          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(MentalCapacityYesNoPage, Yes).success.value
            .set(AddressYesNoPage, false).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = None,
            nationality = None,
            mentalCapacityYesNo = Some(Yes),
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }

        "trustee has Mental Capacity true" in {

          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(MentalCapacityYesNoPage, No).success.value
            .set(AddressYesNoPage, false).success.value
            .set(PassportDetailsYesNoPage, false).success.value
            .set(IdCardDetailsYesNoPage, false).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            countryOfResidence = None,
            nationality = None,
            mentalCapacityYesNo = Some(No),
            address = None,
            entityStart = startDate,
            provisional = true
          )
        }
      }

      "amending" when {

        "trustee has address and combined passport/ID card" in {
          val userAnswers = baseAnswers
            .set(DateOfBirthYesNoPage, false).success.value
            .set(NationalInsuranceNumberYesNoPage, false).success.value
            .set(AddressYesNoPage, true).success.value
            .set(LiveInTheUkYesNoPage, false).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(PassportOrIdCardDetailsYesNoPage, true).success.value
            .set(PassportOrIdCardDetailsPage, combined).success.value
            .set(CountryOfResidenceYesNoPage, true).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
            .set(CountryOfResidencePage, "Spain").success.value
            .set(CountryOfNationalityYesNoPage, true).success.value
            .set(CountryOfNationalityInTheUkYesNoPage, false).success.value
            .set(CountryOfNationalityPage, "Spain").success.value
            .set(MentalCapacityYesNoPage, Yes).success.value
            .set(WhenAddedPage, startDate).success.value

          val result = mapper.map(userAnswers).get

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = None,
            phoneNumber = None,
            identification = Some(combined),
            address = Some(nonUkAddress),
            countryOfResidence = Some("Spain"),
            nationality = Some("Spain"),
            mentalCapacityYesNo = Some(Yes),
            entityStart = startDate,
            provisional = true
          )
        }
      }
    }
  }
}

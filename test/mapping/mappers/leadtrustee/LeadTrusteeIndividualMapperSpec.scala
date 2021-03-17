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
import pages.leadtrustee.individual._

import java.time.LocalDate

class LeadTrusteeIndividualMapperSpec extends SpecBase {

  private val name: Name = Name("First", None, "Last")
  private val dateOfBirth: LocalDate = LocalDate.parse("1996-02-03")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, country)
  private val nino: String = "nino"
  private val email: String = "email"
  private val phone: String = "tel"
  private val combined: CombinedPassportOrIdCard = CombinedPassportOrIdCard("FR", "1234567890", LocalDate.parse("2020-02-03"))

  private val mapper: LeadTrusteeIndividualMapper = new LeadTrusteeIndividualMapper()

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(DateOfBirthPage, dateOfBirth).success.value

  "Lead trustee individual mapper" must {

    "map user answers to lead trustee individual" when {

      "4mld" when {

        "trustee has NINO, UK address and email" in {
          val userAnswers = baseAnswers
            .set(UkCitizenPage, true).success.value
            .set(NationalInsuranceNumberPage, nino).success.value
            .set(LiveInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(EmailAddressYesNoPage, true).success.value
            .set(EmailAddressPage, email).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = dateOfBirth,
            phoneNumber = phone,
            email = Some(email),
            identification = NationalInsuranceNumber(nino),
            address = ukAddress
          )
        }

        "trustee has passport/ID card, non-UK address and no email" in {
          val userAnswers = baseAnswers
            .set(UkCitizenPage, false).success.value
            .set(PassportOrIdCardDetailsPage, combined).success.value
            .set(LiveInTheUkYesNoPage, false).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(EmailAddressYesNoPage, false).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = dateOfBirth,
            phoneNumber = phone,
            email = None,
            identification = combined,
            address = nonUkAddress
          )
        }
      }

      "5mld" when {

        "trustee has UK nationality, NINO, and UK residency/address" in {
          val userAnswers = baseAnswers
            .set(CountryOfNationalityInTheUkYesNoPage, true).success.value
            .set(UkCitizenPage, true).success.value
            .set(NationalInsuranceNumberPage, nino).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(EmailAddressYesNoPage, true).success.value
            .set(EmailAddressPage, email).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = dateOfBirth,
            phoneNumber = phone,
            email = Some(email),
            identification = NationalInsuranceNumber(nino),
            address = ukAddress,
            countryOfResidence = Some(GB),
            nationality = Some(GB)
          )
        }

        "trustee has non-UK nationality, passport/ID card, and non-UK residency/address" in {
          val userAnswers = baseAnswers
            .set(CountryOfNationalityInTheUkYesNoPage, false).success.value
            .set(CountryOfNationalityPage, country).success.value
            .set(UkCitizenPage, false).success.value
            .set(PassportOrIdCardDetailsPage, combined).success.value
            .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
            .set(CountryOfResidencePage, country).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(EmailAddressYesNoPage, false).success.value
            .set(TelephoneNumberPage, phone).success.value

          val result = mapper.map(userAnswers).get

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = dateOfBirth,
            phoneNumber = phone,
            email = None,
            identification = combined,
            address = nonUkAddress,
            countryOfResidence = Some(country),
            nationality = Some(country)
          )
        }
      }
    }
  }
}

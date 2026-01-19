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

package navigation.leadtrustee

import base.SpecBase
import controllers.leadtrustee.individual.routes._
import models.CombinedPassportOrIdCard
import navigation.Navigator
import pages.leadtrustee.individual._

import java.time.LocalDate

class IndividualLeadTrusteeNavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "IndividualLeadTrusteeNavigator" when {

    "Name page -> Date of birth page" in {
      navigator.nextPage(NamePage, emptyUserAnswers)
        .mustBe(DateOfBirthController.onPageLoad())
    }

    "Date of birth page -> UK nationality yes/no page" in {
      navigator.nextPage(DateOfBirthPage, emptyUserAnswers)
        .mustBe(CountryOfNationalityInTheUkYesNoController.onPageLoad())
    }

    "UK nationality yes/no page" when {
      val page = CountryOfNationalityInTheUkYesNoPage

      "-> YES -> NINO yes/no page" in {
        val answers = emptyUserAnswers.set(page, true).success.value

        navigator.nextPage(page, answers)
          .mustBe(UkCitizenController.onPageLoad())
      }

      "-> NO -> Nationality page" in {
        val answers = emptyUserAnswers.set(page, false).success.value

        navigator.nextPage(page, answers)
          .mustBe(CountryOfNationalityController.onPageLoad())
      }
    }

    "Nationality page -> NINO yes/no page" in {
      navigator.nextPage(CountryOfNationalityPage, emptyUserAnswers)
        .mustBe(UkCitizenController.onPageLoad())
    }

    "NINO yes/no page" when {
      val page = UkCitizenPage

      "-> YES -> NINO page" in {
        val answers = emptyUserAnswers.set(page, true).success.value

        navigator.nextPage(page, answers)
          .mustBe(NationalInsuranceNumberController.onPageLoad())
      }

      "-> NO (When Adding or amending in session) -> Passport/ID card page" in {
        val answers = emptyUserAnswers.set(page, false).success.value

        navigator.nextPage(page, answers)
          .mustBe(PassportOrIdCardController.onPageLoad())
      }

      "-> NO (When Amending from ETMP) -> UK residency yes/no page" in {
        val answers = emptyUserAnswers
          .set(page, false).success.value
          .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("GB", "1234567890", LocalDate.now())).success.value

        navigator.nextPage(page, answers)
          .mustBe(CountryOfResidenceInTheUkYesNoController.onPageLoad())
      }
    }

    "NINO page -> UK residency yes/no page" in {
      navigator.nextPage(NationalInsuranceNumberPage, emptyUserAnswers)
        .mustBe(CountryOfResidenceInTheUkYesNoController.onPageLoad())
    }

    "Passport/ID card page -> UK residency yes/no page" in {
      navigator.nextPage(PassportOrIdCardDetailsPage, emptyUserAnswers)
        .mustBe(CountryOfResidenceInTheUkYesNoController.onPageLoad())
    }

    "UK residency yes/no page" when {
      val page = CountryOfResidenceInTheUkYesNoPage

      "-> YES -> UK address page" in {
        val answers = emptyUserAnswers.set(page, true).success.value

        navigator.nextPage(page, answers)
          .mustBe(UkAddressController.onPageLoad())
      }

      "-> NO -> Residency page" in {
        val answers = emptyUserAnswers.set(page, false).success.value

        navigator.nextPage(page, answers)
          .mustBe(CountryOfResidenceController.onPageLoad())
      }
    }

    "Residency page -> Non-UK address page" in {
      navigator.nextPage(CountryOfResidencePage, emptyUserAnswers)
        .mustBe(NonUkAddressController.onPageLoad())
    }

    "UK address page -> Email yes/no page" in {
      navigator.nextPage(UkAddressPage, emptyUserAnswers)
        .mustBe(EmailAddressYesNoController.onPageLoad())
    }

    "Non-UK address page -> Email address yes/no page" in {
      navigator.nextPage(NonUkAddressPage, emptyUserAnswers)
        .mustBe(EmailAddressYesNoController.onPageLoad())
    }

    "Email address yes/no page" when {
      val page = EmailAddressYesNoPage

      "-> YES -> Email address page" in {
        val answers = emptyUserAnswers.set(page, true).success.value

        navigator.nextPage(page, answers)
          .mustBe(EmailAddressController.onPageLoad())
      }

      "-> NO -> Telephone number page" in {
        val answers = emptyUserAnswers.set(page, false).success.value

        navigator.nextPage(page, answers)
          .mustBe(TelephoneNumberController.onPageLoad())
      }
    }

    "Email address page -> Telephone number page" in {
      navigator.nextPage(EmailAddressPage, emptyUserAnswers)
        .mustBe(TelephoneNumberController.onPageLoad())
    }

    "Telephone number page -> Check details page" in {
      navigator.nextPage(TelephoneNumberPage, emptyUserAnswers)
        .mustBe(CheckDetailsController.onPageLoadUpdated())
    }
  }
}

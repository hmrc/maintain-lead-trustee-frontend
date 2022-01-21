/*
 * Copyright 2022 HM Revenue & Customs
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

package pages.trustee.individual

import pages.behaviours.PageBehaviours

class NationalInsuranceNumberYesNoPageSpec extends PageBehaviours {

  "NationalInsuranceNumberYesNoPage" must {

    beRetrievable[Boolean](NationalInsuranceNumberYesNoPage)

    beSettable[Boolean](NationalInsuranceNumberYesNoPage)

    beRemovable[Boolean](NationalInsuranceNumberYesNoPage)

    "implement cleanup logic" when {
      "YES selected" in {
        val userAnswers = emptyUserAnswers
          .set(AddressYesNoPage, true).success.value
          .set(LiveInTheUkYesNoPage, true).success.value
          .set(UkAddressPage, arbitraryUkAddress.arbitrary.sample.get).success.value
          .set(NonUkAddressPage, arbitraryNonUkAddress.arbitrary.sample.get).success.value
          .set(PassportDetailsYesNoPage, true).success.value
          .set(PassportDetailsPage, arbitraryPassport.arbitrary.sample.get).success.value
          .set(IdCardDetailsYesNoPage, true).success.value
          .set(IdCardDetailsPage, arbitraryIdCard.arbitrary.sample.get).success.value
          .set(PassportOrIdCardDetailsYesNoPage, true).success.value
          .set(PassportOrIdCardDetailsPage, arbitraryCombinedPassportOrIdCard.arbitrary.sample.get).success.value

        val result = userAnswers.set(NationalInsuranceNumberYesNoPage, true).success.value

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
      }

      "NO selected" in {
        val userAnswers = emptyUserAnswers
          .set(NationalInsuranceNumberPage, "nino").success.value

        val result = userAnswers.set(NationalInsuranceNumberYesNoPage, false).success.value

        result.get(NationalInsuranceNumberPage) mustBe None
      }
    }
  }
}

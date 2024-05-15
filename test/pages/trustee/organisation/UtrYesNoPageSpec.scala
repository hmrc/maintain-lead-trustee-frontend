/*
 * Copyright 2024 HM Revenue & Customs
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

package pages.trustee.organisation

import pages.behaviours.PageBehaviours

class UtrYesNoPageSpec extends PageBehaviours {

  "UtrYesNoPage" must {

    beRetrievable[Boolean](UtrYesNoPage)

    beSettable[Boolean](UtrYesNoPage)

    beRemovable[Boolean](UtrYesNoPage)

    "implement cleanup logic" when {

      "YES selected" in {
        val userAnswers = emptyUserAnswers
          .set(AddressYesNoPage, true).success.value
          .set(AddressInTheUkYesNoPage, true).success.value
          .set(UkAddressPage, arbitraryUkAddress.arbitrary.sample.get).success.value
          .set(NonUkAddressPage, arbitraryNonUkAddress.arbitrary.sample.get).success.value

        val result = userAnswers.set(UtrYesNoPage, true).success.value

        result.get(AddressYesNoPage) mustBe None
        result.get(AddressInTheUkYesNoPage) mustBe None
        result.get(UkAddressPage) mustBe None
        result.get(NonUkAddressPage) mustBe None
      }

      "NO selected" in {
        val userAnswers = emptyUserAnswers
          .set(UtrPage, "utr").success.value

        val result = userAnswers.set(UtrYesNoPage, false).success.value

        result.get(UtrPage) mustBe None
      }
    }
  }
}

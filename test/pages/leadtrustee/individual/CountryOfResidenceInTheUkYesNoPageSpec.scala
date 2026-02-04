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

package pages.leadtrustee.individual

import models.{NonUkAddress, UkAddress}
import pages.behaviours.PageBehaviours

class CountryOfResidenceInTheUkYesNoPageSpec extends PageBehaviours {

  "CountryOfResidenceInTheUkYesNoPage" must {

    beRetrievable[Boolean](CountryOfResidenceInTheUkYesNoPage)

    beSettable[Boolean](CountryOfResidenceInTheUkYesNoPage)

    beRemovable[Boolean](CountryOfResidenceInTheUkYesNoPage)

    "implement cleanup" when {

      "YES selected" in {
        val userAnswers = emptyUserAnswers
          .set(CountryOfResidencePage, "FR")
          .success
          .value
          .set(NonUkAddressPage, NonUkAddress("Line 1", "Line 2", None, "FR"))
          .success
          .value

        val result = userAnswers.set(CountryOfResidenceInTheUkYesNoPage, true).success.value

        result.get(CountryOfResidencePage) mustBe None
        result.get(NonUkAddressPage)       mustBe None
      }

      "NO selected" in {
        val userAnswers = emptyUserAnswers
          .set(UkAddressPage, UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"))
          .success
          .value

        val result = userAnswers.set(CountryOfResidenceInTheUkYesNoPage, false).success.value

        result.get(UkAddressPage) mustBe None
      }
    }
  }

}

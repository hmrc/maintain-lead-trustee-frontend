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

package pages.trustee.individual

import pages.behaviours.PageBehaviours

class PassportOrIdCardDetailsYesNoPageSpec extends PageBehaviours {

  "PassportOrIdCardDetailsYesNoPage" must {

    beRetrievable[Boolean](PassportOrIdCardDetailsYesNoPage)

    beSettable[Boolean](PassportOrIdCardDetailsYesNoPage)

    beRemovable[Boolean](PassportOrIdCardDetailsYesNoPage)

    "implement cleanup logic" when {
      "NO selected" in {
        val userAnswers = emptyUserAnswers
          .set(PassportOrIdCardDetailsPage, arbitraryCombinedPassportOrIdCard.arbitrary.sample.get).success.value
          .set(PassportOrIdCardDetailsYesNoPage, false).success.value

        userAnswers.get(PassportOrIdCardDetailsPage) mustBe None
      }
    }
  }
}

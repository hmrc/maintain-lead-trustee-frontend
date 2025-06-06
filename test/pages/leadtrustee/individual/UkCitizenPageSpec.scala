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

package pages.leadtrustee.individual

import models.CombinedPassportOrIdCard
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class UkCitizenPageSpec extends PageBehaviours {

  "UkCitizenPage" must {

    beRetrievable[Boolean](UkCitizenPage)

    beSettable[Boolean](UkCitizenPage)

    beRemovable[Boolean](UkCitizenPage)

    "implement cleanup logic when YES selected" in {
      val userAnswers = emptyUserAnswers
        .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("GB", "NUMBER", LocalDate.of(2040, 12, 31)))
        .flatMap(_.set(UkCitizenPage, true))

      userAnswers.get.get(PassportOrIdCardDetailsPage) mustNot be(defined)
    }

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(NationalInsuranceNumberPage, "nino")
        .flatMap(_.set(UkCitizenPage, false))

      userAnswers.get.get(NationalInsuranceNumberPage) mustNot be(defined)
    }
  }
}

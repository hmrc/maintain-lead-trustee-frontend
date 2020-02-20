/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import models.{IdentificationDetailOptions, PassportOrIdCardDetails}
import models.IdentificationDetailOptions._
import pages.behaviours.PageBehaviours

class IdentificationDetailOptionsPageSpec extends PageBehaviours {

  "IdentificationDetailOptionsPage" must {

    beRetrievable[IdentificationDetailOptions](IdentificationDetailOptionsPage)

    beSettable[IdentificationDetailOptions](IdentificationDetailOptionsPage)

    beRemovable[IdentificationDetailOptions](IdentificationDetailOptionsPage)

    "implement cleanup logic when ID-CARD selected" in {
      val userAnswers = emptyUserAnswers
        .set(PassportDetailsPage, "passport")
        .flatMap(_.set(IdentificationDetailOptionsPage, IdCard))

      userAnswers.get.get(PassportDetailsPage) mustNot be(defined)
    }

    "implement cleanup logic when PASSPORT selected" in {
      val userAnswers = emptyUserAnswers
        .set(IdCardDetailsPage, PassportOrIdCardDetails("FR", "987655678", LocalDate.now()))
        .flatMap(_.set(IdentificationDetailOptionsPage, Passport))

      userAnswers.get.get(IdCardDetailsPage) mustNot be(defined)
    }
  }
}

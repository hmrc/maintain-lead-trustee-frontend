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

package pages.leadtrustee.organisation

import models.{NonUkAddress, UkAddress}
import pages.behaviours.PageBehaviours

class BasedInTheUkYesNoPageSpec extends PageBehaviours {

  "RegisteredInUkYesNo page" must {

    beRetrievable[Boolean](BasedInTheUkYesNoPage)

    beSettable[Boolean](BasedInTheUkYesNoPage)

    beRemovable[Boolean](BasedInTheUkYesNoPage)

    "implement cleanup logic when YES selected" in {
      val userAnswers = emptyUserAnswers
        .set(NonUkAddressPage, NonUkAddress("line1", "line2", None, "country"))
        .flatMap(_.set(BasedInTheUkYesNoPage, true))

      userAnswers.get.get(NonUkAddressPage) mustNot be(defined)
    }

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode"))
        .flatMap(_.set(BasedInTheUkYesNoPage, false))

      userAnswers.get.get(UkAddressPage) mustNot be(defined)
    }
  }
}
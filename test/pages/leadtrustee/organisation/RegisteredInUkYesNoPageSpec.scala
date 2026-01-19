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

package pages.leadtrustee.organisation

import pages.behaviours.PageBehaviours

class RegisteredInUkYesNoPageSpec extends PageBehaviours {

  "RegisteredInUkYesNo page" must {

    beRetrievable[Boolean](RegisteredInUkYesNoPage)

    beSettable[Boolean](RegisteredInUkYesNoPage)

    beRemovable[Boolean](RegisteredInUkYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(UtrPage, "1234567890")
        .flatMap(_.set(RegisteredInUkYesNoPage, false))

      userAnswers.get.get(UtrPage) mustBe None
    }
  }
}

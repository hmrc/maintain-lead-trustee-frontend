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

package navigation.trustee

import base.SpecBase
import models.IndividualOrBusiness._
import models.{Mode, NormalMode}
import navigation.Navigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.trustee._

class TrusteeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks {

  val navigator  = new Navigator
  val mode: Mode = NormalMode

  "Trustee navigator" when {

    "Individual or business page -> Individual -> Trustee name page" in {
      val answers = emptyUserAnswers
        .set(IndividualOrBusinessPage, Individual)
        .success
        .value

      navigator
        .nextPage(IndividualOrBusinessPage, mode, answers)
        .mustBe(controllers.trustee.individual.routes.NameController.onPageLoad(mode))
    }

    "Individual or business page -> Business -> Business name page" in {
      val answers = emptyUserAnswers
        .set(IndividualOrBusinessPage, Business)
        .success
        .value

      navigator
        .nextPage(IndividualOrBusinessPage, mode, answers)
        .mustBe(controllers.trustee.organisation.routes.NameController.onPageLoad(mode))
    }
  }

}

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

package navigation

import base.SpecBase
import controllers.routes.IndexController
import generators.Generators
import models.TrusteeType._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  implicit val navigator: Navigator = new Navigator

  "Navigator" when {

    "navigating individual lead trustee change journey" when {
      import controllers.leadtrustee.individual.routes._
      import pages.leadtrustee.individual._

      "navigating away from the trustee name question should go to the Do you know Date of birth question" in {
        val value1 = DateOfBirthController.onPageLoad()
        navigator.nextPage(NamePage, emptyUserAnswers) mustBe value1
      }
    }

    "go to Index from a page that doesn't exist in the route map" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, emptyUserAnswers) mustBe IndexController.onPageLoad("UTRUTRUTR")
    }

    "Lead trustee or trustee page -> Lead trustee -> Lead trustee individual or business page" in {
      val answers = emptyUserAnswers
        .set(TrusteeTypePage, LeadTrustee).success.value

      navigator.nextPage(TrusteeTypePage, answers)
        .mustBe(controllers.leadtrustee.routes.IndividualOrBusinessController.onPageLoad())
    }

    "Lead trustee or trustee page -> Trustee -> Trustee individual or business page" in {
      val answers = emptyUserAnswers
        .set(TrusteeTypePage, Trustee).success.value

      navigator.nextPage(TrusteeTypePage, answers)
        .mustBe(controllers.trustee.routes.IndividualOrBusinessController.onPageLoad())
    }

  }
}

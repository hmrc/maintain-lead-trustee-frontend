/*
 * Copyright 2023 HM Revenue & Customs
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

package navigation.leadtrustee

import base.SpecBase
import controllers.leadtrustee.organisation.routes._
import navigation.Navigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.leadtrustee.organisation._

class OrganisationLeadTrusteeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new Navigator

  "OrganisationLeadTrusteeNavigator" when {

      "UK registered yes/no" when {
        val page = RegisteredInUkYesNoPage

        "-> YES" must {

          val answers = emptyUserAnswers
            .set(page, true).success.value

          "-> Name page" in {
            navigator.nextPage(page, answers)
              .mustBe(NameController.onPageLoad())
          }

          "-> Name Page -> UTR page" in {
            navigator.nextPage(NamePage, answers)
              .mustBe(UtrController.onPageLoad())
          }
        }

        "-> NO" must {

          val answers = emptyUserAnswers
            .set(page, false).success.value

          "-> Name page" in {
            navigator.nextPage(page, answers)
              .mustBe(NameController.onPageLoad())
          }

          "-> Name page -> UK residency yes/no page" in {
            navigator.nextPage(NamePage, answers)
              .mustBe(CountryOfResidenceInTheUkYesNoController.onPageLoad())
          }
        }
      }

      "UTR page -> CountryOfResidenceInTheUk yes/no page" in {
        navigator.nextPage(UtrPage, emptyUserAnswers)
          .mustBe(CountryOfResidenceInTheUkYesNoController.onPageLoad())
      }

      "UK residency yes/no page" when {
        val page = CountryOfResidenceInTheUkYesNoPage

        "-> YES -> UK address page" in {
          val answers = emptyUserAnswers
            .set(page, true).success.value

          navigator.nextPage(page, answers)
            .mustBe(UkAddressController.onPageLoad())
        }

        "-> NO -> Residency page" in {
          val answers = emptyUserAnswers
            .set(page, false).success.value

          navigator.nextPage(page, answers)
            .mustBe(CountryOfResidenceController.onPageLoad())
        }
      }

      "Residency page -> Non-UK address page" in {
        navigator.nextPage(CountryOfResidencePage, emptyUserAnswers)
          .mustBe(NonUkAddressController.onPageLoad())
      }

      "UK address page -> Email address yes/no page" in {
        navigator.nextPage(UkAddressPage, emptyUserAnswers)
          .mustBe(EmailAddressYesNoController.onPageLoad())
      }

      "Non-UK address page -> Email address yes/no page" in {
        navigator.nextPage(NonUkAddressPage, emptyUserAnswers)
          .mustBe(EmailAddressYesNoController.onPageLoad())
      }

      "Email address yes/no page" when {
        val page = EmailAddressYesNoPage

        "-> YES -> Email address page" in {
          val answers = emptyUserAnswers
            .set(page, true).success.value

          navigator.nextPage(page, answers)
            .mustBe(EmailAddressController.onPageLoad())
        }

        "-> NO -> Telephone number page" in {
          val answers = emptyUserAnswers
            .set(page, false).success.value

          navigator.nextPage(page, answers)
            .mustBe(TelephoneNumberController.onPageLoad())
        }
      }

      "Email address page -> Telephone number page" in {
        navigator.nextPage(EmailAddressPage, emptyUserAnswers)
          .mustBe(TelephoneNumberController.onPageLoad())
      }

      "Telephone number page -> Check details page" in {
        navigator.nextPage(TelephoneNumberPage, emptyUserAnswers)
          .mustBe(CheckDetailsController.onPageLoadUpdated())
      }
  }
}

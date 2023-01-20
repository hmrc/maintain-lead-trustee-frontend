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

package navigation.trustee

import base.SpecBase
import controllers.trustee.organisation.add.{routes => addRts}
import controllers.trustee.organisation.amend.{routes => amendRts}
import controllers.trustee.organisation.{routes => rts}
import models.{CheckMode, NormalMode}
import navigation.Navigator
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.trustee.organisation._
import pages.trustee.organisation.add.WhenAddedPage
import pages.trustee.organisation.amend.IndexPage

class OrganisationTrusteeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks {

  val navigator = new Navigator

  "Organisation trustee navigator" when {

    "taxable" must {

      "add journey navigation" must {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)
        val mode = NormalMode

        "Name page -> Do you know UTR page" in {
          navigator.nextPage(NamePage, mode, emptyUserAnswers)
            .mustBe(rts.UtrYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> Yes -> UTR page" in {
          val answers = emptyUserAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(rts.UtrController.onPageLoad(mode))
        }

        "UTR page -> When added as trustee page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers)
            .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Country of Residence Pages" must {

          "Yes No page -> Yes -> Country of Residence In The Uk page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode))
          }

          "Yes No page -> No (with UTR) -> When Added Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, true).success.value
              .set(CountryOfResidenceYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "Yes No page -> No (without UTR) -> Address Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, false).success.value
              .set(CountryOfResidenceYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(rts.AddressYesNoController.onPageLoad(mode))
          }

          "In The Uk page -> Yes (with UTR) -> When Added Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, true).success.value
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "In The Uk page -> Yes (without UTR) -> Address Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, false).success.value
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(rts.AddressYesNoController.onPageLoad(mode))
          }

          "In The Uk page -> No -> Country of Residence page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
          }

          "page (with UTR) -> When Added Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, true).success.value
              .set(CountryOfResidencePage, "ES").success.value

            navigator.nextPage(CountryOfResidencePage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "page (without UTR) -> Address Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, false).success.value
              .set(CountryOfResidencePage, "ES").success.value

            navigator.nextPage(CountryOfResidencePage, mode, answers)
              .mustBe(rts.AddressYesNoController.onPageLoad(mode))
          }
        }

        "Address Pages" must {
          "Do you know address page -> No -> When Added page" in {
            val answers = baseAnswers
              .set(AddressYesNoPage, false).success.value

            navigator.nextPage(AddressYesNoPage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "UK address page -> When Added page" in {
            navigator.nextPage(UkAddressPage, mode, baseAnswers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "Non-UK address page -> When Added page" in {
            navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "When added page -> Check your answers page" in {
            navigator.nextPage(WhenAddedPage, mode, baseAnswers)
              .mustBe(addRts.CheckDetailsController.onPageLoad())
          }
        }
      }

      "amend journey navigation" must {

        val index = 0
        val mode = CheckMode
        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)
          .set(IndexPage, index).success.value

        "Name page -> Do you know UTR page" in {
          navigator.nextPage(NamePage, mode, emptyUserAnswers)
            .mustBe(rts.UtrYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> Yes -> UTR page" in {
          val answers = emptyUserAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(rts.UtrController.onPageLoad(mode))
        }

        "UTR page -> When added as trustee page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers)
            .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Country of Residence Pages" must {

          "Yes No page -> Yes -> Country of Residence In The Uk page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode))
          }

          "Yes No page -> No (with UTR) -> check details Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, true).success.value
              .set(CountryOfResidenceYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "Yes No page -> No (without UTR) -> Address Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, false).success.value
              .set(CountryOfResidenceYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(rts.AddressYesNoController.onPageLoad(mode))
          }

          "In The Uk page -> Yes (with UTR) -> check details Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, true).success.value
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "In The Uk page -> Yes (without UTR) -> Address Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, false).success.value
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(rts.AddressYesNoController.onPageLoad(mode))
          }

          "In The Uk page -> No -> Country of Residence page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
          }

          "page (with UTR) -> Check details Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, true).success.value
              .set(CountryOfResidencePage, "ES").success.value

            navigator.nextPage(CountryOfResidencePage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "page (without UTR) -> Address Page" in {
            val answers = baseAnswers
              .set(UtrYesNoPage, false).success.value
              .set(CountryOfResidencePage, "ES").success.value

            navigator.nextPage(CountryOfResidencePage, mode, answers)
              .mustBe(rts.AddressYesNoController.onPageLoad(mode))
          }
        }

        "Address Pages" must {
          "Do you know address page -> No -> check details page" in {
            val answers = baseAnswers
              .set(AddressYesNoPage, false).success.value

            navigator.nextPage(AddressYesNoPage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "UK address page -> check details page" in {
            navigator.nextPage(UkAddressPage, mode, baseAnswers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "Non-UK address page -> check details page" in {
            navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }
        }
      }
    }

    "non-taxable" must {

      "add journey navigation" must {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)
        val mode = NormalMode

        "Name page -> Country of Residence Yes No page" in {
          navigator.nextPage(NamePage, mode, baseAnswers)
            .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Country of Residence Pages" must {

          "Yes No page -> Yes -> Country of Residence In The Uk page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode))
          }

          "Yes No page -> No -> When Added Page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "In The Uk page -> Yes -> When Added Page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "In The Uk page -> No -> Country of Residence page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
          }

          "page -> When Added Page" in {
            val answers = baseAnswers
              .set(CountryOfResidencePage, "ES").success.value

            navigator.nextPage(CountryOfResidencePage, mode, answers)
              .mustBe(addRts.WhenAddedController.onPageLoad())
          }

          "When added page -> Check your answers page" in {
            navigator.nextPage(WhenAddedPage, mode, baseAnswers)
              .mustBe(addRts.CheckDetailsController.onPageLoad())
          }
        }
      }

      "amend journey navigation" must {

        val index = 0
        val mode = CheckMode
        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)
          .set(IndexPage, index).success.value

        "Name page -> Country of Residence Yes No page" in {
          navigator.nextPage(NamePage, mode, baseAnswers)
            .mustBe(rts.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Country of Residence Pages" must {

          "Yes No page -> Yes -> Country of Residence In The Uk page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode))
          }

          "Yes No page -> No -> check details Page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "In The Uk page -> Yes -> check details Page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }

          "In The Uk page -> No -> Country of Residence page" in {
            val answers = baseAnswers
              .set(CountryOfResidenceYesNoPage, true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage, false).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage, mode, answers)
              .mustBe(rts.CountryOfResidenceController.onPageLoad(mode))
          }

          "page -> check details Page" in {
            val answers = baseAnswers
              .set(CountryOfResidencePage, "ES").success.value

            navigator.nextPage(CountryOfResidencePage, mode, answers)
              .mustBe(amendRts.CheckDetailsController.onPageLoadUpdated(index))
          }
        }
      }
    }
  }
}

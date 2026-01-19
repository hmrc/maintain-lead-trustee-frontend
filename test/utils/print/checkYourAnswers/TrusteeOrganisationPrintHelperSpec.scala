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

package utils.print.checkYourAnswers

import base.SpecBase
import controllers.trustee.organisation.add.{routes => addRts}
import controllers.trustee.organisation.{routes => rts}
import models.IndividualOrBusiness.Business
import models.{CheckMode, Name, NonUkAddress, NormalMode, UkAddress}
import pages.trustee.IndividualOrBusinessPage
import pages.trustee.organisation._
import pages.trustee.organisation.add._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class TrusteeOrganisationPrintHelperSpec extends SpecBase {

  val name: Name = Name("First", Some("Middle"), "Last")
  val trusteeUkAddress: UkAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  val trusteeNonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  "TrusteeIndividualPrintHelper" must {

    "generate individual trustee section for all possible data" when {

      val helper = injector.instanceOf[TrusteeOrganisationPrintHelper]

      val userAnswers = emptyUserAnswers
        .set(IndividualOrBusinessPage, Business).success.value
        .set(NamePage, name.displayName).success.value
        .set(UtrYesNoPage, true).success.value
        .set(UtrPage, "1234567890").success.value
        .set(AddressYesNoPage, true).success.value
        .set(AddressInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, trusteeUkAddress).success.value
        .set(NonUkAddressPage, trusteeNonUkAddress).success.value
        .set(WhenAddedPage, LocalDate.of(2020, 1, 1)).success.value

      "adding" in {

        val mode = NormalMode

        val result = helper.print(userAnswers, adding = true, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("trustee.organisation.name.checkYourAnswersLabel"), answer = Html("First Last"), changeUrl = Some(rts.NameController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.utrYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.UtrYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.utr.checkYourAnswersLabel", name.displayName), answer = Html("1234567890"), changeUrl = Some(rts.UtrController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.AddressYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.addressInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.AddressInTheUkYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(rts.UkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(rts.NonUkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.whenAdded.checkYourAnswersLabel", name.displayName), answer = Html("1 January 2020"), changeUrl = Some(addRts.WhenAddedController.onPageLoad().url))
          )
        )
      }

      "amending" in {

        val mode = CheckMode

        val result = helper.print(userAnswers, adding = false, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("trustee.organisation.name.checkYourAnswersLabel"), answer = Html("First Last"), changeUrl = Some(rts.NameController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.utrYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.UtrYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.utr.checkYourAnswersLabel", name.displayName), answer = Html("1234567890"), changeUrl = Some(rts.UtrController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.addressYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.AddressYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.addressInTheUkYesNo.checkYourAnswersLabel", name.displayName), answer = Html("Yes"), changeUrl = Some(rts.AddressInTheUkYesNoController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.ukAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(rts.UkAddressController.onPageLoad(mode).url)),
            AnswerRow(label = messages("trustee.organisation.nonUkAddress.checkYourAnswersLabel", name.displayName), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(rts.NonUkAddressController.onPageLoad(mode).url))
          )
        )
      }
    }
  }
}

/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.Inject
import controllers.trustee.organisation.add.routes._
import controllers.trustee.organisation.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.trustee.organisation._
import pages.trustee.organisation.add._
import play.api.i18n.Messages
import utils.print.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}

class TrusteeOrganisationPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def print(userAnswers: UserAnswers, adding: Boolean, trusteeName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, trusteeName)

    val prefix: String = "trustee.organisation"

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (adding) NormalMode else CheckMode
      Seq(
        bound.stringQuestion(NamePage, s"$prefix.name", NameController.onPageLoad(mode).url),
        bound.yesNoQuestion(UtrYesNoPage, s"$prefix.utrYesNo", UtrYesNoController.onPageLoad(mode).url),
        bound.stringQuestion(UtrPage, s"$prefix.utr", UtrController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfResidenceYesNoPage, s"$prefix.countryOfResidenceYesNo", CountryOfResidenceYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfResidenceInTheUkYesNoPage, s"$prefix.countryOfResidenceInTheUkYesNo", CountryOfResidenceInTheUkYesNoController.onPageLoad(mode).url),
        bound.countryQuestion(CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, s"$prefix.countryOfResidence", CountryOfResidenceController.onPageLoad(mode).url),
        bound.yesNoQuestion(AddressYesNoPage, s"$prefix.addressYesNo", AddressYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(AddressInTheUkYesNoPage, s"$prefix.addressInTheUkYesNo", AddressInTheUkYesNoController.onPageLoad(mode).url),
        bound.addressQuestion(UkAddressPage, s"$prefix.ukAddress", UkAddressController.onPageLoad(mode).url),
        bound.addressQuestion(NonUkAddressPage, s"$prefix.nonUkAddress", NonUkAddressController.onPageLoad(mode).url),
        if (adding) bound.dateQuestion(WhenAddedPage, "trustee.whenAdded", WhenAddedController.onPageLoad().url) else None
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }
}

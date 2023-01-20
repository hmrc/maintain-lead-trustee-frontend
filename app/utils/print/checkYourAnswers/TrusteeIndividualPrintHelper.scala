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

package utils.print.checkYourAnswers

import com.google.inject.Inject
import controllers.trustee.individual.add.routes._
import controllers.trustee.individual.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.trustee.individual._
import pages.trustee.individual.add._
import play.api.i18n.Messages
import utils.print.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}

class TrusteeIndividualPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def print(userAnswers: UserAnswers, adding: Boolean, name: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, name)

    val changeLinkOrNone: (Boolean, String) => Option[String] =
      (adding: Boolean, route: String) => if(adding) Some(route) else None

    val prefix: String = "trustee.individual"

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (adding) NormalMode else CheckMode
      Seq(
        bound.nameQuestion(NamePage, s"$prefix.name", NameController.onPageLoad(mode).url),
        bound.yesNoQuestion(DateOfBirthYesNoPage, s"$prefix.dateOfBirthYesNo", DateOfBirthYesNoController.onPageLoad(mode).url),
        bound.dateQuestion(DateOfBirthPage, s"$prefix.dateOfBirth", DateOfBirthController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfNationalityYesNoPage, s"$prefix.countryOfNationalityYesNo", CountryOfNationalityYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfNationalityInTheUkYesNoPage, s"$prefix.countryOfNationalityInTheUkYesNo", CountryOfNationalityInTheUkYesNoController.onPageLoad(mode).url),
        bound.countryQuestion(CountryOfNationalityInTheUkYesNoPage, CountryOfNationalityPage, s"$prefix.countryOfNationality", CountryOfNationalityController.onPageLoad(mode).url),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, s"$prefix.nationalInsuranceNumberYesNo", NationalInsuranceNumberYesNoController.onPageLoad(mode).url),
        bound.ninoQuestion(NationalInsuranceNumberPage, s"$prefix.nationalInsuranceNumber", NationalInsuranceNumberController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfResidenceYesNoPage, s"$prefix.countryOfResidenceYesNo", CountryOfResidenceYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfResidenceInTheUkYesNoPage, s"$prefix.countryOfResidenceInTheUkYesNo", CountryOfResidenceInTheUkYesNoController.onPageLoad(mode).url),
        bound.countryQuestion(CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, s"$prefix.countryOfResidence", CountryOfResidenceController.onPageLoad(mode).url),
        bound.yesNoQuestion(AddressYesNoPage, s"$prefix.addressYesNo", AddressYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, s"$prefix.liveInTheUkYesNo", LiveInTheUkYesNoController.onPageLoad(mode).url),
        bound.addressQuestion(UkAddressPage, s"$prefix.ukAddress", UkAddressController.onPageLoad(mode).url),
        bound.addressQuestion(NonUkAddressPage, s"$prefix.nonUkAddress", NonUkAddressController.onPageLoad(mode).url),
        bound.yesNoQuestion(PassportDetailsYesNoPage, s"$prefix.passportDetailsYesNo", PassportDetailsYesNoController.onPageLoad(mode).url),
        bound.passportDetailsQuestion(PassportDetailsPage, s"$prefix.passportDetails", PassportDetailsController.onPageLoad(mode).url),
        bound.yesNoQuestion(IdCardDetailsYesNoPage, s"$prefix.idCardDetailsYesNo", IdCardDetailsYesNoController.onPageLoad(mode).url),
        bound.idCardDetailsQuestion(IdCardDetailsPage, s"$prefix.idCardDetails", IdCardDetailsController.onPageLoad(mode).url),
        bound.yesNoQuestion(PassportOrIdCardDetailsYesNoPage, s"$prefix.passportOrIdCardDetailsYesNo", changeUrl = changeLinkOrNone(adding, PassportOrIdCardDetailsYesNoController.onPageLoad(mode).url), canEdit = adding),
        bound.passportOrIdCardDetailsQuestion(PassportOrIdCardDetailsPage, s"$prefix.passportOrIdCardDetails", changeUrl = changeLinkOrNone(adding, PassportOrIdCardDetailsController.onPageLoad(mode).url), canEdit = adding),
        bound.enumQuestion(MentalCapacityYesNoPage, s"$prefix.mentalCapacityYesNo", MentalCapacityYesNoController.onPageLoad(mode).url, "site"),
        if (adding) bound.dateQuestion(WhenAddedPage, "trustee.whenAdded", WhenAddedController.onPageLoad.url) else None
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)
  }
}

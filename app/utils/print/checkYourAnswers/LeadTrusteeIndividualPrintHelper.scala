/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.leadtrustee.individual.routes._
import models.UserAnswers
import pages.leadtrustee.individual._
import play.api.i18n.Messages
import queries.Gettable
import utils.print.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}

class LeadTrusteeIndividualPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def print(userAnswers: UserAnswers, trusteeName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, trusteeName)

    val isProvisional: Boolean = userAnswers.get(PassportOrIdCardDetailsPage) match {
      case Some(value) => value.detailsType.isProvisional
      case None => false
    }

    val changeLinkOrNone: (Boolean, String) => Option[String] =
      (adding: Boolean, route: String) => if(adding) Some(route) else None

    val prefix: String = "leadtrustee.individual"

    val isLeadTrusteeMatched = userAnswers.isLeadTrusteeMatched

    val inUkQuestion: (Gettable[Boolean], String, String, Boolean) => Option[AnswerRow] =
      if (isLeadTrusteeMatched) bound.yesNoQuestionAllowEmptyAnswer else bound.yesNoQuestion

    def answerRows: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, s"$prefix.name", NameController.onPageLoad().url, canEdit = !isLeadTrusteeMatched),
      bound.dateQuestion(DateOfBirthPage, s"$prefix.dateOfBirth", DateOfBirthController.onPageLoad().url, canEdit = !isLeadTrusteeMatched),
      inUkQuestion(CountryOfNationalityInTheUkYesNoPage, s"$prefix.countryOfNationalityInTheUkYesNo", CountryOfNationalityInTheUkYesNoController.onPageLoad().url, true),
      bound.countryQuestion(CountryOfNationalityInTheUkYesNoPage, CountryOfNationalityPage, s"$prefix.countryOfNationality", CountryOfNationalityController.onPageLoad().url),
      bound.yesNoQuestion(UkCitizenPage, s"$prefix.ukCitizen", UkCitizenController.onPageLoad().url, canEdit = !isLeadTrusteeMatched),
      bound.ninoQuestion(NationalInsuranceNumberPage, s"$prefix.nationalInsuranceNumber", NationalInsuranceNumberController.onPageLoad().url, canEdit = !isLeadTrusteeMatched),
      bound.passportOrIdCardDetailsQuestion(PassportOrIdCardDetailsPage, s"$prefix.passportOrIdCardDetails", changeLinkOrNone(isProvisional, PassportOrIdCardController.onPageLoad().url), canEdit = isProvisional),
      inUkQuestion(CountryOfResidenceInTheUkYesNoPage, s"$prefix.countryOfResidenceInTheUkYesNo", CountryOfResidenceInTheUkYesNoController.onPageLoad().url, true),
      bound.countryQuestion(CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, s"$prefix.countryOfResidence", CountryOfResidenceController.onPageLoad().url),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, s"$prefix.liveInTheUkYesNo", LiveInTheUkYesNoController.onPageLoad().url),
      bound.addressQuestion(UkAddressPage, s"$prefix.ukAddress", UkAddressController.onPageLoad().url),
      bound.addressQuestion(NonUkAddressPage, s"$prefix.nonUkAddress", NonUkAddressController.onPageLoad().url),
      bound.yesNoQuestion(EmailAddressYesNoPage, s"$prefix.emailAddressYesNo", EmailAddressYesNoController.onPageLoad().url),
      bound.stringQuestion(EmailAddressPage, s"$prefix.emailAddress", EmailAddressController.onPageLoad().url),
      bound.stringQuestion(TelephoneNumberPage, s"$prefix.telephoneNumber", TelephoneNumberController.onPageLoad().url)
    ).flatten

    AnswerSection(headingKey = None, rows = answerRows)
  }
}

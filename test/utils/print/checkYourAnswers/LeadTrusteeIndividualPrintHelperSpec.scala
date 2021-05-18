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

import java.time.LocalDate
import base.SpecBase
import controllers.leadtrustee.individual.routes._
import models.BpMatchStatus.FullyMatched
import models.{CombinedPassportOrIdCard, Name, NonUkAddress, UkAddress, UserAnswers}
import pages.leadtrustee.individual._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class LeadTrusteeIndividualPrintHelperSpec extends SpecBase {

  val name: Name = Name("Lead", None, "Trustee")
  val ukAddress: UkAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  val country: String = "DE"
  val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, country)
  val nino = "AA000000A"

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(DateOfBirthPage, LocalDate.of(1996, 2, 3)).success.value
    .set(CountryOfNationalityInTheUkYesNoPage, false).success.value
    .set(CountryOfNationalityPage, country).success.value
    .set(UkCitizenPage, true).success.value
    .set(NationalInsuranceNumberPage, nino).success.value
    .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("DE", "number", LocalDate.of(1996, 2, 3))).success.value
    .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
    .set(CountryOfResidencePage, country).success.value
    .set(LiveInTheUkYesNoPage, true).success.value
    .set(UkAddressPage, ukAddress).success.value
    .set(NonUkAddressPage, nonUkAddress).success.value
    .set(EmailAddressYesNoPage, true).success.value
    .set(EmailAddressPage, "email").success.value
    .set(TelephoneNumberPage, "tel").success.value

  "LeadTrusteeIndividualPrintHelper" must {

    "generate lead trustee organisation section" when {

      "not fully matched" in {

        val helper = injector.instanceOf[LeadTrusteeIndividualPrintHelper]

        val result = helper.print(baseAnswers, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = Html(messages("leadtrustee.individual.name.checkYourAnswersLabel")), answer = Html("Lead Trustee"), changeUrl = NameController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("3 February 1996"), changeUrl = DateOfBirthController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfNationalityInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("No"), changeUrl = CountryOfNationalityInTheUkYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfNationality.checkYourAnswersLabel", name.displayName)), answer = Html("Germany"), changeUrl = CountryOfNationalityController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.ukCitizen.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = UkCitizenController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = NationalInsuranceNumberController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.passportOrIdCardDetails.checkYourAnswersLabel", name.displayName)), answer = Html("Germany<br />number<br />3 February 1996"), changeUrl = PassportOrIdCardController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("No"), changeUrl = CountryOfResidenceInTheUkYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfResidence.checkYourAnswersLabel", name.displayName)), answer = Html("Germany"), changeUrl = CountryOfResidenceController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = LiveInTheUkYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = UkAddressController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = NonUkAddressController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.emailAddressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = EmailAddressYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.emailAddress.checkYourAnswersLabel", name.displayName)), answer = Html("email"), changeUrl = EmailAddressController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.telephoneNumber.checkYourAnswersLabel", name.displayName)), answer = Html("tel"), changeUrl = TelephoneNumberController.onPageLoad().url)
          )
        )
      }

      "fully matched" in {

        val helper = injector.instanceOf[LeadTrusteeIndividualPrintHelper]

        val userAnswers = baseAnswers.copy(is5mldEnabled = true)
          .set(BpMatchStatusPage, FullyMatched).success.value

        val result = helper.print(userAnswers, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = Html(messages("leadtrustee.individual.name.checkYourAnswersLabel")), answer = Html("Lead Trustee"), changeUrl = NameController.onPageLoad().url, canEdit = false, isVerified = true),
            AnswerRow(label = Html(messages("leadtrustee.individual.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("3 February 1996"), changeUrl = DateOfBirthController.onPageLoad().url, canEdit = false, isVerified = true),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfNationalityInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("No"), changeUrl = CountryOfNationalityInTheUkYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfNationality.checkYourAnswersLabel", name.displayName)), answer = Html("Germany"), changeUrl = CountryOfNationalityController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.ukCitizen.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = UkCitizenController.onPageLoad().url, canEdit = false),
            AnswerRow(label = Html(messages("leadtrustee.individual.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = NationalInsuranceNumberController.onPageLoad().url, canEdit = false, isVerified = true),
            AnswerRow(label = Html(messages("leadtrustee.individual.passportOrIdCardDetails.checkYourAnswersLabel", name.displayName)), answer = Html("Germany<br />number<br />3 February 1996"), changeUrl = PassportOrIdCardController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("No"), changeUrl = CountryOfResidenceInTheUkYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.countryOfResidence.checkYourAnswersLabel", name.displayName)), answer = Html("Germany"), changeUrl = CountryOfResidenceController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = LiveInTheUkYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = UkAddressController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = NonUkAddressController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.emailAddressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = EmailAddressYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.emailAddress.checkYourAnswersLabel", name.displayName)), answer = Html("email"), changeUrl = EmailAddressController.onPageLoad().url),
            AnswerRow(label = Html(messages("leadtrustee.individual.telephoneNumber.checkYourAnswersLabel", name.displayName)), answer = Html("tel"), changeUrl = TelephoneNumberController.onPageLoad().url)
          )
        )
      }

      "no nationality or residency" when {

        "lead trustee matched" must {
          "render rows with empty answers" in {

            val helper = injector.instanceOf[LeadTrusteeIndividualPrintHelper]

            val userAnswers = emptyUserAnswers.copy(is5mldEnabled = true)
              .set(BpMatchStatusPage, FullyMatched).success.value
              .set(NationalInsuranceNumberPage, nino).success.value

            val result = helper.print(userAnswers, name.displayName)
            result mustBe AnswerSection(
              headingKey = None,
              rows = Seq(
                AnswerRow(label = Html(messages("leadtrustee.individual.countryOfNationalityInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html(""), changeUrl = CountryOfNationalityInTheUkYesNoController.onPageLoad().url),
                AnswerRow(label = Html(messages("leadtrustee.individual.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = NationalInsuranceNumberController.onPageLoad().url, canEdit = false, isVerified = true),
                AnswerRow(label = Html(messages("leadtrustee.individual.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html(""), changeUrl = CountryOfResidenceInTheUkYesNoController.onPageLoad().url)
              )
            )
          }
        }

        "lead trustee not matched" must {
          "not render rows with empty answers" in {

            val helper = injector.instanceOf[LeadTrusteeIndividualPrintHelper]

            val userAnswers = emptyUserAnswers

            val result = helper.print(userAnswers, name.displayName)
            result mustBe AnswerSection(
              headingKey = None,
              rows = Seq()
            )
          }
        }
      }
    }
  }
}

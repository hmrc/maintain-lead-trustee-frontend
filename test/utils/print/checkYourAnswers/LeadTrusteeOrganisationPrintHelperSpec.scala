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
import controllers.leadtrustee.organisation.routes._
import models.{NonUkAddress, UkAddress}
import pages.leadtrustee.organisation._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class LeadTrusteeOrganisationPrintHelperSpec extends SpecBase {

  val name: String = "Lead Trustee"
  val ukAddress: UkAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  val country: String = "DE"
  val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, country)

  "LeadTrusteeOrganisationPrintHelper" must {

    "generate lead trustee organisation section for all possible data" in {

      val helper = injector.instanceOf[LeadTrusteeOrganisationPrintHelper]

      val userAnswers = emptyUserAnswers
        .set(RegisteredInUkYesNoPage, true).success.value
        .set(NamePage, name).success.value
        .set(UtrPage, "utr").success.value
        .set(CountryOfResidenceInTheUkYesNoPage, false).success.value
        .set(CountryOfResidencePage, country).success.value
        .set(AddressInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, ukAddress).success.value
        .set(NonUkAddressPage, nonUkAddress).success.value
        .set(EmailAddressYesNoPage, true).success.value
        .set(EmailAddressPage, "email").success.value
        .set(TelephoneNumberPage, "tel").success.value

      val result = helper.print(userAnswers, name)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = messages("leadtrustee.organisation.registeredInUkYesNo.checkYourAnswersLabel"), answer = Html("Yes"), changeUrl = Some(RegisteredInUkYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.name.checkYourAnswersLabel"), answer = Html("Lead Trustee"), changeUrl = Some(NameController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.utr.checkYourAnswersLabel", name), answer = Html("utr"), changeUrl = Some(UtrController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = Some(CountryOfResidenceInTheUkYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.countryOfResidence.checkYourAnswersLabel", name), answer = Html("Germany"), changeUrl = Some(CountryOfResidenceController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.addressInTheUkYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = Some(AddressInTheUkYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.ukAddress.checkYourAnswersLabel", name), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = Some(UkAddressController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.nonUkAddress.checkYourAnswersLabel", name), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = Some(NonUkAddressController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.emailAddressYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = Some(EmailAddressYesNoController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.emailAddress.checkYourAnswersLabel", name), answer = Html("email"), changeUrl = Some(EmailAddressController.onPageLoad().url)),
          AnswerRow(label = messages("leadtrustee.organisation.telephoneNumber.checkYourAnswersLabel", name), answer = Html("tel"), changeUrl = Some(TelephoneNumberController.onPageLoad().url))
        )
      )
    }
  }
}

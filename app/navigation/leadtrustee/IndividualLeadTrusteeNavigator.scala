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

package navigation.leadtrustee

import controllers.leadtrustee.individual.routes._
import models.UserAnswers
import pages.Page
import pages.leadtrustee.individual._
import play.api.mvc.Call

object IndividualLeadTrusteeNavigator extends LeadTrusteeNavigator {

  val routes: PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation orElse
      yesNoNavigation

  private def simpleNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => DateOfBirthController.onPageLoad()
    case DateOfBirthPage => _ => CountryOfNationalityInTheUkYesNoController.onPageLoad()
    case CountryOfNationalityPage => _ => UkCitizenController.onPageLoad()
    case NationalInsuranceNumberPage | PassportOrIdCardDetailsPage => _ => CountryOfResidenceInTheUkYesNoController.onPageLoad()
    case CountryOfResidencePage => _ => NonUkAddressController.onPageLoad()
    case UkAddressPage | NonUkAddressPage => _ => EmailAddressYesNoController.onPageLoad()
    case EmailAddressPage => _ => TelephoneNumberController.onPageLoad()
    case TelephoneNumberPage => _ => CheckDetailsController.onPageLoadUpdated()
  }

  private def yesNoNavigation: PartialFunction[Page, UserAnswers => Call] =
    yesNoNav(CountryOfNationalityInTheUkYesNoPage, UkCitizenController.onPageLoad(), CountryOfNationalityController.onPageLoad()) orElse
      yesNoNav(UkCitizenPage, NationalInsuranceNumberController.onPageLoad(), PassportOrIdCardController.onPageLoad()) orElse
      yesNoNav(CountryOfResidenceInTheUkYesNoPage, UkAddressController.onPageLoad(), CountryOfResidenceController.onPageLoad()) orElse
      yesNoNav(LiveInTheUkYesNoPage, UkAddressController.onPageLoad(), NonUkAddressController.onPageLoad()) orElse
      yesNoNav(EmailAddressYesNoPage, EmailAddressController.onPageLoad(), TelephoneNumberController.onPageLoad())

}

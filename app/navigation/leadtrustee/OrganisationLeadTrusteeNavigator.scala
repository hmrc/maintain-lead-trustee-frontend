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

package navigation.leadtrustee

import controllers.leadtrustee.organisation.routes._
import models.UserAnswers
import pages.Page
import pages.leadtrustee.organisation._
import play.api.mvc.Call

object OrganisationLeadTrusteeNavigator extends LeadTrusteeNavigator {

  val routes: PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation orElse
      yesNoNavigation orElse
      conditionalNavigation

  private def simpleNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case UtrPage => _ => CountryOfResidenceInTheUkYesNoController.onPageLoad()
    case CountryOfResidencePage => _ => NonUkAddressController.onPageLoad()
    case UkAddressPage | NonUkAddressPage => _ => EmailAddressYesNoController.onPageLoad()
    case EmailAddressPage => _ => TelephoneNumberController.onPageLoad()
    case TelephoneNumberPage => _ => CheckDetailsController.onPageLoadUpdated()
  }

  private def yesNoNavigation: PartialFunction[Page, UserAnswers => Call] =
    yesNoNav(RegisteredInUkYesNoPage, NameController.onPageLoad(), NameController.onPageLoad()) orElse
      yesNoNav(CountryOfResidenceInTheUkYesNoPage, UkAddressController.onPageLoad(), CountryOfResidenceController.onPageLoad()) orElse
      yesNoNav(AddressInTheUkYesNoPage, UkAddressController.onPageLoad(), NonUkAddressController.onPageLoad()) orElse
      yesNoNav(EmailAddressYesNoPage, EmailAddressController.onPageLoad(), TelephoneNumberController.onPageLoad())

  private def conditionalNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => navigateToUtrQuestionIfUkRegistered
  }

  private def navigateToUtrQuestionIfUkRegistered(userAnswers: UserAnswers): Call = {
    userAnswers.get(RegisteredInUkYesNoPage).map {
      case true => UtrController.onPageLoad()
      case false => CountryOfResidenceInTheUkYesNoController.onPageLoad()
    }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }

}

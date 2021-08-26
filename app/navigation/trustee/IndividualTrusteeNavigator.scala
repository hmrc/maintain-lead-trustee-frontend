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

package navigation.trustee

import controllers.trustee.individual.add.{routes => addRts}
import controllers.trustee.individual.amend.{routes => amendRts}
import controllers.trustee.individual.{routes => rts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.trustee.individual._
import pages.trustee.individual.add._
import pages.trustee.individual.amend._
import play.api.mvc.Call

object IndividualTrusteeNavigator extends TrusteeNavigator {

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    linearNavigation(mode) orElse
      yesNoNavigation(mode) orElse
      addNavigation(mode) orElse
      amendNavigation(mode)

  private def linearNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => rts.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage => navigateAwayFromDateOfBirthPages(_, mode)
    case CountryOfNationalityPage => navigateAwayFromCountryOfNationalityPages(_, mode)
    case NationalInsuranceNumberPage => navigateAwayFromNinoPages(_, mode)
    case CountryOfResidencePage => navigateToOrBypassAddressPages(_, mode)
    case UkAddressPage | NonUkAddressPage => navigateToIdQuestions(_, mode)
    case PassportDetailsPage | IdCardDetailsPage | PassportOrIdCardDetailsPage => navigateToMentalCapacityOrWhenAddedPage(_, mode)
    case MentalCapacityYesNoPage => navigateToWhenAddedOrCheckDetails(_, mode)
    case WhenAddedPage => _ => addRts.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = DateOfBirthYesNoPage,
      yesCall = rts.DateOfBirthController.onPageLoad(mode),
      noCall = navigateAwayFromDateOfBirthPages(ua, mode)
    )
    case CountryOfNationalityYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = CountryOfNationalityYesNoPage,
      yesCall = rts.CountryOfNationalityInTheUkYesNoController.onPageLoad(mode),
      noCall = navigateAwayFromCountryOfNationalityPages(ua, mode)
    )
    case CountryOfNationalityInTheUkYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = CountryOfNationalityInTheUkYesNoPage,
      yesCall = navigateAwayFromCountryOfNationalityPages(ua, mode),
      noCall = rts.CountryOfNationalityController.onPageLoad(mode)
    )
    case NationalInsuranceNumberYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = NationalInsuranceNumberYesNoPage,
      yesCall = rts.NationalInsuranceNumberController.onPageLoad(mode),
      noCall = navigateAwayFromNinoPages(ua, mode)
    )
    case CountryOfResidenceYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = CountryOfResidenceYesNoPage,
      yesCall = rts.CountryOfResidenceInTheUkYesNoController.onPageLoad(mode),
      noCall = navigateToOrBypassAddressPages(ua, mode)
    )
    case CountryOfResidenceInTheUkYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = CountryOfResidenceInTheUkYesNoPage,
      yesCall = navigateToOrBypassAddressPages(ua, mode),
      noCall = rts.CountryOfResidenceController.onPageLoad(mode)
    )
    case AddressYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = AddressYesNoPage,
      yesCall = rts.LiveInTheUkYesNoController.onPageLoad(mode),
      noCall = navigateToMentalCapacityOrWhenAddedPage(ua, mode)
    )
    case LiveInTheUkYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = LiveInTheUkYesNoPage,
      yesCall = rts.UkAddressController.onPageLoad(mode),
      noCall = rts.NonUkAddressController.onPageLoad(mode)
    )
  }

  private def addNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case PassportDetailsYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = PassportDetailsYesNoPage,
      yesCall = rts.PassportDetailsController.onPageLoad(mode),
      noCall = rts.IdCardDetailsYesNoController.onPageLoad(mode)
    )
    case IdCardDetailsYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = IdCardDetailsYesNoPage,
      yesCall = rts.IdCardDetailsController.onPageLoad(mode),
      noCall = navigateToMentalCapacityOrWhenAddedPage(ua, mode)
    )
  }

  private def amendNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case PassportOrIdCardDetailsYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = PassportOrIdCardDetailsYesNoPage,
      yesCall = rts.PassportOrIdCardDetailsController.onPageLoad(mode),
      noCall = navigateToMentalCapacityOrWhenAddedPage(ua, mode)
    )
  }

  private def navigateAwayFromNinoPages(ua: UserAnswers, mode: Mode): Call = {
    if (ua.is5mldEnabled) {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    } else {
      navigateToOrBypassAddressPages(ua, mode)
    }
  }

  private def navigateToOrBypassAddressPages(ua: UserAnswers, mode: Mode): Call = {
    if (ua.get(NationalInsuranceNumberPage).isDefined || !ua.isTaxable) {
      navigateToMentalCapacityOrWhenAddedPage(ua, mode)
    } else {
      rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromDateOfBirthPages(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.is5mldEnabled) {
      rts.CountryOfNationalityYesNoController.onPageLoad(mode)
    } else {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromCountryOfNationalityPages(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.isTaxable) {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    } else {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    }
  }

  private def navigateToIdQuestions(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(PassportOrIdCardDetailsPage).isDefined) {
      rts.PassportOrIdCardDetailsYesNoController.onPageLoad(mode)
    } else {
      rts.PassportDetailsYesNoController.onPageLoad(mode)
    }
  }

  private def navigateToMentalCapacityOrWhenAddedPage(userAnswers: UserAnswers, mode: Mode): Call  = {
    if (userAnswers.is5mldEnabled) {
      rts.MentalCapacityYesNoController.onPageLoad(mode)
    } else {
      navigateToWhenAddedOrCheckDetails(userAnswers, mode)
    }
  }

  private def navigateToWhenAddedOrCheckDetails(userAnswers: UserAnswers, mode: Mode): Call = {
    if (mode == NormalMode) {
      addRts.WhenAddedController.onPageLoad()
    } else {
      checkDetailsNavigation(userAnswers)
    }
  }

  private def checkDetailsNavigation(userAnswers: UserAnswers): Call = {
    userAnswers.get(IndexPage) match {
      case Some(index) => amendRts.CheckDetailsController.onPageLoadUpdated(index)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}

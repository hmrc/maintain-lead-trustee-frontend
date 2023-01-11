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

package navigation.leadtrustee

import models.IndividualOrBusiness.{Business, Individual}
import models.UserAnswers
import pages.{Page, QuestionPage}
import pages.leadtrustee.IndividualOrBusinessPage
import play.api.mvc.Call

trait LeadTrusteeNavigator {

  def yesNoNav(fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): PartialFunction[Page, UserAnswers => Call] = {
    case `fromPage` => ua =>
      ua.get(fromPage)
        .map(if (_) yesCall else noCall)
        .getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }

  def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }
}

object LeadTrusteeNavigator {

  val routes: PartialFunction[Page, UserAnswers => Call] =
    parameterisedNavigation orElse
      IndividualLeadTrusteeNavigator.routes orElse
      OrganisationLeadTrusteeNavigator.routes

  private def parameterisedNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case IndividualOrBusinessPage => individualOrBusinessNavigation
  }

  private def individualOrBusinessNavigation(userAnswers: UserAnswers): Call = {
    userAnswers.get(IndividualOrBusinessPage).map {
      case Individual => controllers.leadtrustee.individual.routes.NameController.onPageLoad()
      case Business => controllers.leadtrustee.organisation.routes.RegisteredInUkYesNoController.onPageLoad()
    }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }
}

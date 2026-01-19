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

package navigation.trustee

import models.IndividualOrBusiness.{Business, Individual}
import models.{Mode, UserAnswers}
import pages.{Page, QuestionPage}
import pages.trustee.IndividualOrBusinessPage
import play.api.mvc.Call

trait TrusteeNavigator {

  def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }
}

object TrusteeNavigator {

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    parameterisedNavigation(mode) orElse
      IndividualTrusteeNavigator.routes(mode) orElse
      OrganisationTrusteeNavigator.routes(mode)

  private def parameterisedNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case IndividualOrBusinessPage => individualOrBusinessNavigation(_, mode)
  }

  private def individualOrBusinessNavigation(userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(IndividualOrBusinessPage).map {
      case Individual => controllers.trustee.individual.routes.NameController.onPageLoad(mode)
      case Business => controllers.trustee.organisation.routes.NameController.onPageLoad(mode)
    }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }
}

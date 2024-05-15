/*
 * Copyright 2024 HM Revenue & Customs
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

package navigation

import models.TrusteeType._
import models.{Mode, NormalMode, UserAnswers}
import navigation.leadtrustee.LeadTrusteeNavigator
import navigation.trustee.TrusteeNavigator
import pages.{Page, TrusteeTypePage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    normalRoutes(mode)(page)(userAnswers)

  private def normalRoutes(mode: Mode): Page => UserAnswers => Call =
    parameterisedNavigation orElse
      LeadTrusteeNavigator.routes orElse
      TrusteeNavigator.routes(mode) orElse {
      case _ => ua => controllers.routes.IndexController.onPageLoad(ua.identifier)
    }

  private def parameterisedNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case TrusteeTypePage => trusteeTypeNavigation
  }

  private def trusteeTypeNavigation(userAnswers: UserAnswers): Call = {
    userAnswers.get(TrusteeTypePage).map {
      case LeadTrustee => controllers.leadtrustee.routes.IndividualOrBusinessController.onPageLoad()
      case Trustee => controllers.trustee.routes.IndividualOrBusinessController.onPageLoad()
    }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad)
  }

}

/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import models.TrusteeType._
import models.UserAnswers
import navigation.leadtrustee.LeadTrusteeNavigator
import navigation.trustee.TrusteeNavigator
import pages.{Page, TrusteeTypePage}
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  private val parameterisedNavigation : PartialFunction[Page, UserAnswers => Call] = {
    case TrusteeTypePage => trusteeTypeNavigation()
  }

  private val normalRoutes: Page => UserAnswers => Call =
    parameterisedNavigation orElse
    LeadTrusteeNavigator.routes orElse
    TrusteeNavigator.routes orElse {
    case _ => ua => controllers.routes.IndexController.onPageLoad(ua.utr)
  }

  def nextPage(page: Page, userAnswers: UserAnswers): Call =
      normalRoutes(page)(userAnswers)

  private def trusteeTypeNavigation()(userAnswers: UserAnswers): Call = {
    userAnswers.get(TrusteeTypePage).map {
      case LeadTrustee => controllers.leadtrustee.routes.IndividualOrBusinessController.onPageLoad()
      case Trustee => controllers.trustee.routes.IndividualOrBusinessController.onPageLoad(0)
    }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
  }

}
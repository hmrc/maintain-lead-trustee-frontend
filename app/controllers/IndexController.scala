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

package controllers

import java.time.LocalDate

import connectors.TrustConnector
import controllers.actions.IdentifierAction
import javax.inject.Inject
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identifierAction: IdentifierAction,
                                 repo : PlaybackRepository,
                                 connector: TrustConnector)
                               (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(utr: String): Action[AnyContent] =

    identifierAction.async {
      implicit request =>
        (connector.getTrustStartDate(utr) flatMap { date =>
          repo.set(UserAnswers(
            request.user.internalId,
            utr,
            LocalDate.parse(date.startDate)
          )).map(_ =>
            Redirect(controllers.leadtrustee.routes.CheckDetailsController.onPageLoad())
          )
        }).recover {case _ => InternalServerError}
    }
}

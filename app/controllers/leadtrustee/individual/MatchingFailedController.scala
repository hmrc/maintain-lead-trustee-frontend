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

package controllers.leadtrustee.individual

import config.FrontendAppConfig
import controllers.actions.StandardActionSets
import handlers.ErrorHandler
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrustsIndividualCheckService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.leadtrustee.individual.MatchingFailedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MatchingFailedController @Inject()(
                                          val controllerComponents: MessagesControllerComponents,
                                          implicit val frontendAppConfig: FrontendAppConfig,
                                          standardActionSets: StandardActionSets,
                                          view: MatchingFailedView,
                                          errorHandler: ErrorHandler,
                                          service: TrustsIndividualCheckService
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private def actions() =
    standardActionSets.verifiedForUtr

  def onPageLoad(): Action[AnyContent] = actions().async {
    implicit request =>

      service.failedAttempts(request.userAnswers.identifier) map {
        case x if x < frontendAppConfig.maxMatchingAttempts =>
          Ok(view(x, frontendAppConfig.maxMatchingAttempts - x))
        case _ =>
          Redirect(routes.MatchingLockedController.onPageLoad())
      } recoverWith {
        case e =>
          logger.error(s"Failed to retrieve number of failed matching attempts: ${e.getMessage}")
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }

  def onSubmit(): Action[AnyContent] = actions() {
      Redirect(routes.NameController.onPageLoad())
  }
}

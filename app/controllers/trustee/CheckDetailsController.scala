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

package controllers.trustee

import config.FrontendAppConfig
import connectors.TrustConnector
import controllers.actions._
import controllers.trustee.actions.NameRequiredAction
import handlers.ErrorHandler
import mapping.mappers.TrusteeMappers
import models.IndividualOrBusiness._
import models.Trustee
import pages.trustee.IndividualOrBusinessPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.checkYourAnswers.TrusteePrintHelpers
import views.html.trustee.CheckDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        nameAction: NameRequiredAction,
                                        printHelper: TrusteePrintHelpers,
                                        mapper: TrusteeMappers,
                                        trustConnector: TrustConnector,
                                        val appConfig: FrontendAppConfig,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      request.userAnswers.get(IndividualOrBusinessPage) match {
        case Some(Individual) =>
          Ok(view(printHelper.printIndividualTrustee(request.userAnswers, provisional = true, request.trusteeName)))
        case Some(Business) =>
          Ok(view(printHelper.printOrganisationTrustee(request.userAnswers, provisional = true, request.trusteeName)))
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}] unable to display trustee on check your answers")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      val trustee: Option[Trustee] = request.userAnswers.get(IndividualOrBusinessPage) match {
        case Some(Individual) =>
          mapper.mapToTrusteeIndividual(request.userAnswers)
        case Some(Business) =>
          mapper.mapToTrusteeOrganisation(request.userAnswers)
        case _ =>
          None
      }

      trustee match {
        case Some(t) =>
          trustConnector.addTrustee(request.userAnswers.identifier, t).map(_ =>
            Redirect(controllers.routes.AddATrusteeController.onPageLoad())
          )
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}] unable to submit trustee on check your answers")
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }
}

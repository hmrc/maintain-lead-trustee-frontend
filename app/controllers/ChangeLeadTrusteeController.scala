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

package controllers

import controllers.actions.StandardActionSets
import handlers.ErrorHandler
import models.{AllTrustees, Trustee, TrusteeIndividual, TrusteeOrganisation, YesNoDontKnow}
import pages.leadtrustee.IsReplacingLeadTrusteePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangeLeadTrusteeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeLeadTrusteeController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  trustService: TrustService,
  standardActionSets: StandardActionSets,
  playbackRepository: PlaybackRepository,
  view: ChangeLeadTrusteeView,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async { implicit request =>
    val logInfo = s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]"

    trustService.getAllTrustees(request.userAnswers.identifier).flatMap { case AllTrustees(_, trustees) =>
      val eligibleToPromote: Seq[Trustee] = trustees.filter {
        case ti: TrusteeIndividual  => ti.mentalCapacityYesNo.contains(YesNoDontKnow.Yes)
        case _: TrusteeOrganisation => true
      }

      logger.info(
        s"$logInfo Found ${trustees.length} total trustees, ${eligibleToPromote.length} eligible for promotion"
      )

      if (eligibleToPromote.nonEmpty) {
        logger.info(s"$logInfo Redirecting to select replacement lead trustee from existing trustees")
        Future.successful(Redirect(controllers.routes.ReplacingLeadTrusteeController.onPageLoad()))
      } else {
        logger.info(s"$logInfo No eligible trustees to promote, redirecting to add new lead trustee")
        val updatedAnswers = request.userAnswers.set(IsReplacingLeadTrusteePage, true)
        for {
          ua <- Future.fromTry(updatedAnswers)
          _  <- playbackRepository.set(ua)
        } yield Redirect(controllers.leadtrustee.routes.IndividualOrBusinessController.onPageLoad())
      }
    } recoverWith { case e =>
      logger.error(s"$logInfo Problem getting trustees for lead trustee change: ${e.getMessage}")
      errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
    }
  }

}

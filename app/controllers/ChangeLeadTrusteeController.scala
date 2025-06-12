/*
 * Copyright 2025 HM Revenue & Customs
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
import models.{AllTrustees, Trustee, TrusteeIndividual, TrusteeOrganisation, YesNoDontKnow}
import pages.leadtrustee.individual.IsReplacingLeadTrusteePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangeLeadTrusteeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeLeadTrusteeController @Inject()(
                                          val controllerComponents: MessagesControllerComponents,
                                          trustService: TrustService,
                                          standardActionSets: StandardActionSets,
                                          playbackRepository: PlaybackRepository,
                                          view: ChangeLeadTrusteeView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async { implicit request =>
    trustService.getAllTrustees(request.userAnswers.identifier).flatMap {
      case AllTrustees(_, trustees) =>
        val eligibleToPromote: Seq[Trustee] = trustees.filter {
          case ti: TrusteeIndividual   => ti.mentalCapacityYesNo.contains(YesNoDontKnow.Yes)
          case _: TrusteeOrganisation   => true
        }

        if (eligibleToPromote.nonEmpty) {
          Future.successful(Redirect(controllers.routes.ReplacingLeadTrusteeController.onPageLoad()))
        } else {
          val updatedAnswers = request.userAnswers.set(IsReplacingLeadTrusteePage, true)
          for {
            ua <- Future.fromTry(updatedAnswers)
            _ <- playbackRepository.set(ua)
          } yield {
            Redirect(controllers.leadtrustee.routes.IndividualOrBusinessController.onPageLoad())
          }
        }
    }
  }
}
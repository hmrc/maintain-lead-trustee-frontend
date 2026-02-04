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

package controllers.leadtrustee.individual

import controllers.actions.StandardActionSets
import controllers.leadtrustee.actions.NameRequiredAction
import pages.leadtrustee.individual.UkCitizenPage
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.leadtrustee.individual.MatchingLockedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MatchingLockedController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  playbackRepository: PlaybackRepository,
  view: MatchingLockedView,
  nameRequiredAction: NameRequiredAction,
  standardActionSets: StandardActionSets
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private def actions() =
    standardActionSets.identifiedUserWithData andThen nameRequiredAction

  def onPageLoad(): Action[AnyContent] = actions() { implicit request =>
    Ok(view(request.leadTrusteeName))
  }

  def continue(): Action[AnyContent] = actions().async { implicit request =>
    for {
      ninoYesNoSet <- Future.fromTry(request.userAnswers.set(UkCitizenPage, false))
      _            <- playbackRepository.set(ninoYesNoSet)
    } yield Redirect(routes.PassportOrIdCardController.onPageLoad())
  }

}

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

package controllers.trustee.individual

import controllers.actions._
import controllers.trustee.actions.NameRequiredAction
import forms.YesNoDontKnowFormProvider
import models.{Mode, YesNoDontKnow}
import navigation.Navigator
import pages.trustee.individual.MentalCapacityYesNoPage
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.trustee.individual.MentalCapacityYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MentalCapacityYesNoController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  repository: PlaybackRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  formProvider: YesNoDontKnowFormProvider,
  view: MentalCapacityYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form: Form[YesNoDontKnow] = formProvider.withPrefix("trustee.individual.mentalCapacityYesNo")

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MentalCapacityYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.trusteeName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.verifiedForUtr.andThen(nameAction).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.trusteeName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(MentalCapacityYesNoPage, value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(MentalCapacityYesNoPage, mode, updatedAnswers))
        )
    }

}

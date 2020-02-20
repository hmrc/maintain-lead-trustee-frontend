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

package controllers.leadtrustee.individual

import controllers.actions._
import controllers.leadtrustee.individual.actions.NameRequiredAction
import forms.NationalInsuranceNumberFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.leadtrustee.individual.NationalInsuranceNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.leadtrustee.individual.NationalInsuranceNumberView

import scala.concurrent.{ExecutionContext, Future}

class NationalInsuranceNumberController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        navigator: Navigator,
                                        standardActionSets: StandardActionSets,
                                        nameAction: NameRequiredAction,
                                        formProvider: NationalInsuranceNumberFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: NationalInsuranceNumberView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider.withPrefix("leadtrustee.individual.nationalInsuranceNumber")

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.IdentifiedUserWithData andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NationalInsuranceNumberPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.leadTrusteeName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.IdentifiedUserWithData andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.leadTrusteeName))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NationalInsuranceNumberPage, value))
            _              <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NationalInsuranceNumberPage, mode, updatedAnswers))
      )
  }
}

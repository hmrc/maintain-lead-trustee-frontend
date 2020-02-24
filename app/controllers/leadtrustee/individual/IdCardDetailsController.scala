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
import forms.IdCardDetailsFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.leadtrustee.individual.IdCardDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.leadtrustee.individual.IdCardDetailsView

import scala.concurrent.{ExecutionContext, Future}

class IdCardDetailsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         playbackRepository: PlaybackRepository,
                                         navigator: Navigator,
                                         standardActionSets: StandardActionSets,
                                         nameAction: NameRequiredAction,
                                         formProvider: IdCardDetailsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: IdCardDetailsView,
                                         countryOptions: CountryOptions
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider.withPrefix("leadtrustee")

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IdCardDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.leadTrusteeName, countryOptions.options))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.leadTrusteeName, countryOptions.options))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IdCardDetailsPage, value))
            _ <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IdCardDetailsPage, mode, updatedAnswers))
      )
  }
}

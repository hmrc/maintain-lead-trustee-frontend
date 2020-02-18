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
import forms.leadtrustee.individual.EmailAddressYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.leadtrustee.individual.EmailAddressYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.leadtrustee.individual.EmailAddressYesNoView
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class EmailAddressYesNoController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         playbackRepository: PlaybackRepository,
                                         navigator: Navigator,
                                         standardActionSets: StandardActionSets,
                                         formProvider: EmailAddressYesNoFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: EmailAddressYesNoView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.IdentifiedUserWithData {
    implicit request =>

      val preparedForm = request.userAnswers.get(EmailAddressYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.IdentifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailAddressYesNoPage, value))
            _              <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EmailAddressYesNoPage, mode, updatedAnswers))
      )
  }
}

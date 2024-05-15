/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.leadtrustee.organisation

import controllers.actions._
import forms.BusinessNameFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.leadtrustee.organisation.{NamePage, RegisteredInUkYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.leadtrustee.organisation.NameView

import scala.concurrent.{ExecutionContext, Future}

class NameController @Inject()(
                                override val messagesApi: MessagesApi,
                                playbackRepository: PlaybackRepository,
                                navigator: Navigator,
                                standardActionSets: StandardActionSets,
                                formProvider: BusinessNameFormProvider,
                                val controllerComponents: MessagesControllerComponents,
                                view: NameView
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider.withPrefix("leadtrustee.organisation.name")

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(NamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(RegisteredInUkYesNoPage) map { answer =>
        Ok(view(preparedForm, answer))
      } getOrElse Redirect(routes.RegisteredInUkYesNoController.onPageLoad())

  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful {
            request.userAnswers.get(RegisteredInUkYesNoPage) map { answer =>
              BadRequest(view(formWithErrors, answer))
            } getOrElse Redirect(routes.RegisteredInUkYesNoController.onPageLoad())
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NamePage, value))
            _ <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NamePage, updatedAnswers))
      )
  }
}

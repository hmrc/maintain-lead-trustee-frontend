/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.trustee.organisation

import controllers.actions.StandardActionSets
import controllers.trustee.actions.{NameRequiredAction, TrusteeNameRequest}
import forms.UtrFormProvider
import models.Mode
import navigation.Navigator
import pages.trustee.organisation.UtrPage
import pages.trustee.organisation.amend.IndexPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.trustee.organisation.UtrView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UtrController @Inject()(
                               override val messagesApi: MessagesApi,
                               registrationsRepository: PlaybackRepository,
                               navigator: Navigator,
                               standardActionSets: StandardActionSets,
                               nameAction: NameRequiredAction,
                               formProvider: UtrFormProvider,
                               val controllerComponents: MessagesControllerComponents,
                               view: UtrView,
                               trustsService: TrustService
                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(utrs: List[String])(implicit request: TrusteeNameRequest[AnyContent]): Form[String] =
    formProvider.apply("trustee.organisation.utr", request.userAnswers.identifier, utrs)

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      val index = request.userAnswers.get(IndexPage)

      trustsService.getBusinessUtrs(request.userAnswers.identifier, index, adding = index.isEmpty) map { utrs =>
        val preparedForm = request.userAnswers.get(UtrPage) match {
          case None => form(utrs)
          case Some(value) => form(utrs).fill(value)
        }

        Ok(view(preparedForm, mode, request.trusteeName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      val index = request.userAnswers.get(IndexPage)

      trustsService.getBusinessUtrs(request.userAnswers.identifier, index, adding = index.isEmpty) flatMap { utrs =>
        form(utrs).bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, mode, request.trusteeName))),

          value => {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UtrPage, value))
              _ <- registrationsRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(UtrPage, mode, updatedAnswers))
          }
        )
      }
  }
}

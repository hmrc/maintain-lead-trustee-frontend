/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.NonUkAddressFormProvider
import models.{Mode, NonUkAddress}

import javax.inject.Inject
import navigation.Navigator
import pages.trustee.individual.NonUkAddressPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.trustee.individual.NonUkAddressView

import scala.concurrent.{ExecutionContext, Future}

class NonUkAddressController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: PlaybackRepository,
                                        navigator: Navigator,
                                        standardActionSets: StandardActionSets,
                                        nameAction: NameRequiredAction,
                                        formProvider: NonUkAddressFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: NonUkAddressView,
                                        val countryOptions: CountryOptionsNonUK
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[NonUkAddress] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NonUkAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, countryOptions.options, request.trusteeName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, countryOptions.options, request.trusteeName))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NonUkAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NonUkAddressPage, mode, updatedAnswers))
      )
  }
}

/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.trustee.individual.amend

import controllers.actions._
import controllers.trustee.actions.NameRequiredAction
import forms.CombinedPassportOrIdCardDetailsFormProvider
import models.{CheckMode, CombinedPassportOrIdCard}
import navigation.Navigator
import pages.trustee.individual.amend.PassportOrIdCardDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.trustee.individual.amend.PassportOrIdCardDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PassportOrIdCardDetailsController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   playbackRepository: PlaybackRepository,
                                                   navigator: Navigator,
                                                   standardActionSets: StandardActionSets,
                                                   nameAction: NameRequiredAction,
                                                   formProvider: CombinedPassportOrIdCardDetailsFormProvider,
                                                   countryOptions: CountryOptions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: PassportOrIdCardDetailsView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[CombinedPassportOrIdCard] = formProvider.withPrefix("trustee.individual.passportOrIdCardDetails")

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PassportOrIdCardDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.trusteeName, countryOptions.options))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.trusteeName, countryOptions.options))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PassportOrIdCardDetailsPage, value))
            _              <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PassportOrIdCardDetailsPage, CheckMode, updatedAnswers))
      )
  }
}

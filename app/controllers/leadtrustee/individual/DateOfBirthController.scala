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
import controllers.leadtrustee.actions.{LeadTrusteeNameRequest, NameRequiredAction}
import forms.DateOfBirthFormProvider

import javax.inject.Inject
import navigation.Navigator
import pages.leadtrustee.individual.DateOfBirthPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.leadtrustee.individual.DateOfBirthView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DateOfBirthController @Inject() (
  override val messagesApi: MessagesApi,
  playbackRepository: PlaybackRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  formProvider: DateOfBirthFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DateOfBirthView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private def form: Form[LocalDate] =
    formProvider.withConfig("leadtrustee.individual.dateOfBirth", matchingLeadTrustee = true)

  private def isLeadTrusteeMatched(implicit request: LeadTrusteeNameRequest[_]) =
    request.userAnswers.isLeadTrusteeMatched

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) { implicit request =>
    val preparedForm = request.userAnswers.get(DateOfBirthPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, request.leadTrusteeName, isLeadTrusteeMatched))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.leadTrusteeName, isLeadTrusteeMatched))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfBirthPage, value))
            _              <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DateOfBirthPage, updatedAnswers))
      )
  }

}

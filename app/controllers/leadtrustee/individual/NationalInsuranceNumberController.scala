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

package controllers.leadtrustee.individual

import controllers.actions._
import controllers.leadtrustee.actions.NameRequiredAction
import forms.NationalInsuranceNumberFormProvider
import handlers.ErrorHandler
import models.BpMatchStatus.FullyMatched
import models.{LockedMatchResponse, ServiceNotIn5mldModeResponse, SuccessfulMatchResponse, UnsuccessfulMatchResponse}
import navigation.Navigator
import pages.leadtrustee.individual.{BpMatchStatusPage, NationalInsuranceNumberPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustsIndividualCheckService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.leadtrustee.individual.NationalInsuranceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class NationalInsuranceNumberController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   playbackRepository: PlaybackRepository,
                                                   navigator: Navigator,
                                                   standardActionSets: StandardActionSets,
                                                   nameAction: NameRequiredAction,
                                                   service: TrustsIndividualCheckService,
                                                   formProvider: NationalInsuranceNumberFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   errorHandler: ErrorHandler,
                                                   view: NationalInsuranceNumberView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = formProvider.withPrefix("leadtrustee.individual.nationalInsuranceNumber")

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NationalInsuranceNumberPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.leadTrusteeName))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.leadTrusteeName))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NationalInsuranceNumberPage, value))
            matchingResponse <- service.matchLeadTrustee(updatedAnswers)
            updatedAnswersWithMatched <- Future.fromTry {
              if (matchingResponse == SuccessfulMatchResponse) {
                updatedAnswers.set(BpMatchStatusPage, FullyMatched)
              } else {
                Success(updatedAnswers)
              }
            }
            _ <- playbackRepository.set(updatedAnswersWithMatched)
          } yield matchingResponse match {
            case SuccessfulMatchResponse | ServiceNotIn5mldModeResponse =>
              Redirect(navigator.nextPage(NationalInsuranceNumberPage, updatedAnswersWithMatched))
            case UnsuccessfulMatchResponse =>
              Redirect(routes.MatchingFailedController.onPageLoad())
            case LockedMatchResponse =>
              Redirect(routes.MatchingLockedController.onPageLoad())
            case _ =>
              InternalServerError(errorHandler.internalServerErrorTemplate)
          }
      )
  }
}

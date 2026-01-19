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

package controllers.trustee.individual.amend

import config.FrontendAppConfig
import connectors.TrustConnector
import controllers.actions._
import controllers.trustee.actions.NameRequiredAction
import handlers.ErrorHandler
import mapping.extractors.TrusteeExtractors
import mapping.mappers.TrusteeMappers
import models.requests.DataRequest
import models.{Trustee, TrusteeIndividual, UserAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.checkYourAnswers.TrusteePrintHelpers
import viewmodels.AnswerSection
import views.html.trustee.individual.amend.CheckDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: PlaybackRepository,
                                        trustService: TrustService,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        printHelper: TrusteePrintHelpers,
                                        mapper: TrusteeMappers,
                                        extractor: TrusteeExtractors,
                                        trustConnector: TrustConnector,
                                        nameAction: NameRequiredAction,
                                        val appConfig: FrontendAppConfig,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {


  private def logInfo(implicit request: DataRequest[AnyContent]): String =
    s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]"

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>
      trustService.getTrustee(request.userAnswers.identifier, index)
        .flatMap {
        case ind: TrusteeIndividual =>
          val answers = extractor.extractTrusteeIndividual(request.userAnswers, ind, index)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _ <- sessionRepository.set(updatedAnswers)
          } yield {
            renderTrustee(updatedAnswers, index, ind.name.displayName)
          }
        case _ =>
          logger.error(s"$logInfo Expected trustee to be of type TrusteeIndividual")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      } recoverWith  {
        case e =>
          logger.error(s"$logInfo Unable to retrieve trustee from trusts: ${e.getMessage}")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }
  }

  def onPageLoadUpdated(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      renderTrustee(request.userAnswers, index, request.trusteeName)(request.request)
  }

  private def renderTrustee(userAnswers: UserAnswers, index: Int, name: String)
                           (implicit request: DataRequest[AnyContent]): Result = {
    val section: AnswerSection =
      printHelper.printIndividualTrustee(
      userAnswers = userAnswers,
      adding = false,
      name = name
    )

    Ok(view(section, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      mapper.mapToTrusteeIndividual(request.userAnswers) match {
        case Some(t) =>
          amendTrustee(request.userAnswers, t, index)
        case _ =>
          logger.error(s"$logInfo Unable to amend trustee")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }
  }

  private def amendTrustee(userAnswers: UserAnswers, t: Trustee, index: Int)
                          (implicit hc: HeaderCarrier): Future[Result] = {
    for {
      _ <- trustConnector.amendTrustee(userAnswers.identifier, index, t)
      updatedUserAnswers <- Future.fromTry(userAnswers.deleteAtPath(pages.trustee.basePath))
      _ <- sessionRepository.set(updatedUserAnswers)
    } yield Redirect(controllers.routes.AddATrusteeController.onPageLoad())
  }

}

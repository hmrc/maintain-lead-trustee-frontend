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

package controllers.leadtrustee.individual

import com.google.inject.Inject
import connectors.TrustConnector
import controllers.actions.StandardActionSets
import controllers.leadtrustee.actions.NameRequiredAction
import handlers.ErrorHandler
import mapping.extractors.TrusteeExtractors
import mapping.mappers.TrusteeMappers
import models.requests.DataRequest
import models.{LeadTrusteeIndividual, UserAnswers}
import pages.leadtrustee.IsReplacingLeadTrusteePage
import pages.leadtrustee.individual.IndexPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.checkYourAnswers.TrusteePrintHelpers
import viewmodels.AnswerSection
import views.html.leadtrustee.individual.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        connector: TrustConnector,
                                        extractor: TrusteeExtractors,
                                        printHelper: TrusteePrintHelpers,
                                        mapper: TrusteeMappers,
                                        repository: PlaybackRepository,
                                        nameAction: NameRequiredAction,
                                        errorHandler: ErrorHandler
                                      )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private def logInfo(implicit request: DataRequest[AnyContent]): String =
    s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]"

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      connector.getLeadTrustee(request.userAnswers.identifier).flatMap {
        case ind: LeadTrusteeIndividual =>
          val answers: Try[UserAnswers] = extractor.extractLeadTrusteeIndividual(request.userAnswers, ind)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _ <- repository.set(updatedAnswers)
          } yield {
            renderLeadTrustee(updatedAnswers, ind.name.displayName)
          }
        case _ =>
          logger.error(s"$logInfo Expected lead trustee to be of type LeadTrusteeIndividual")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      } recoverWith  {
        case e =>
          logger.error(s"$logInfo Unable to retrieve Lead Trustee from trusts: ${e.getMessage}")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }
  }

  def onPageLoadUpdated(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      renderLeadTrustee(request.userAnswers, request.leadTrusteeName)(request.request)
  }

  private def renderLeadTrustee(userAnswers: UserAnswers, name: String)(implicit request: DataRequest[AnyContent]): Result = {
    val section: AnswerSection = printHelper.printLeadIndividualTrustee(
      userAnswers = userAnswers,
      name = name
    )

    Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>
      val userAnswers = request.userAnswers
      val identifier = userAnswers.identifier
      mapper.mapToLeadTrusteeIndividual(userAnswers) match {
        case Some(leadTrustee) =>
          connectorCall(userAnswers, identifier, leadTrustee).flatMap {
            case Right(response) =>
              submitTransform(() => Future.successful(response), userAnswers)
            case Left(error) =>
              logger.error(s"$logInfo [CheckDetailsController][onSubmit] Failed to update the lead trustee due to : $error")
              errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
          }
        case _ =>
          logger.error(s"$logInfo [CheckDetailsController][onSubmit] Unable to build lead trustee individual from user answers. Cannot continue with submitting transform.")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }
  }

  private def submitTransform(transform: () => Future[HttpResponse], userAnswers: UserAnswers): Future[Result] = {
    logger.info("[CheckDetailsController][submitTransform] Deleting lead trustee from user answers for Individual")
    for {
      _ <- transform()
      cleanedAnswers <- Future.fromTry(userAnswers.remove(IsReplacingLeadTrusteePage))
      updatedUserAnswers <- Future.fromTry(cleanedAnswers.deleteAtPath(pages.leadtrustee.basePath))
      _ <- repository.set(updatedUserAnswers)
    } yield Redirect(controllers.routes.AddATrusteeController.onPageLoad())
  }

  private def connectorCall(userAnswers: UserAnswers,
                             identifier: String,
                             leadTrustee: LeadTrusteeIndividual
                           )(implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[Either[String, HttpResponse]] = {
    val indexPage = userAnswers.get(IndexPage)
    val call: Future[HttpResponse] = indexPage match {
      case Some(index) =>
        logger.info(s"$logInfo [CheckDetailsController][connectorCall] Promoting lead trustee at index $index")
        connector.promoteTrustee(identifier, index, leadTrustee)
      case None =>
        userAnswers.get(IsReplacingLeadTrusteePage) match {
          case Some(true) =>
            logger.info(s"$logInfo [CheckDetailsController][connectorCall] Adding new lead trustee to replace existing")
            connector.demoteLeadTrustee(userAnswers.identifier, leadTrustee)
          case _ =>
            logger.info(s"$logInfo [CheckDetailsController][connectorCall] Amending lead trustee")
            connector.amendLeadTrustee(userAnswers.identifier, leadTrustee)
        }
    }

    call.map { response =>
      response.status match {
        case OK => Right(response)
        case _ => Left(s"$logInfo [CheckDetailsController][connectorCall] Connector call failed with status ${response.status}")
      }
    }.recover {
      case ex =>
        Left(s"$logInfo [CheckDetailsController][connectorCall] Connector call failed with exception: ${ex.getMessage}")
    }
  }
}
